package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.IOException;
import java.util.Date; // TODO: You'll likely use this in this class
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import static gitlet.Repository.CWD;

import static gitlet.Repository.stagingArea;
import static gitlet.Repository.head;
import static gitlet.Repository.HEAD;

import static gitlet.Utils.*;


/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  a commit class basically creates a snapshot of the current state of my repo
 *  and stores the commit object in memory.
 *  does at a high level.
 *
 *  @author Ganesh
 */
public class Commit implements java.io.Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    public String parent;
    public String message;
    public Date date;
    public TreeMap<String, String> tree;

    //todo : make a separate constructor for init
    public Commit(String m) {
        message = m;
        date = new Date();
    }

    // persists commit to memory
    public void saveCommit() {

        if (!head.equals("")){
            String parentSha = readObject(join(CWD, ".gitlet/branches", head), String.class);
            Commit parentCommit = getCommit(parentSha);
            parent = sha1((Object) serialize(parentCommit));
            tree = parentCommit.tree;
        }

        try {
            updateTree();
            String hashName = sha1((Object) serialize(this));
            File commit = join(CWD, "/.gitlet/objects/commits", hashName);
            commit.createNewFile();
            writeObject(commit, this);
            updateBranch(hashName);
        }catch (IOException e){
            System.out.println("error while creating commit object.");
        }

    }

    // updates the head for branch and also the head file in .gitlets/
    //todo : refactor to keep only one ref to in branches and let head point to branches
    private void updateBranch(String newHead) {
        List<String> branches = plainFilenamesIn(join(CWD, ".gitlet/branches"));
        assert branches != null;
        File branchObject = null;
        for (String branch : branches){
            branchObject = join(CWD, ".gitlet/branches", branch);
            if (branch.equals(head)){
                break;
            }
        }
        Utils.writeObject(branchObject, newHead);
    }


    // retreives commit object from memory
    public static Commit getCommit(String sha1){
        if (sha1 == null){
            return null;
        }
        File commitFile = join(CWD, "/.gitlet/objects/commits", sha1);
        return readObject(commitFile, Commit.class);
    }

    private void updateTree(){
        if (tree == null && parent == null){
            tree = new TreeMap<>();
            return;
        }

        tree = stagingArea.updateTree(tree);
    }

    public String getDate(){
        return date.toString();
    }

    public String getMessage(){
        return message;
    }

    public boolean hasFile(String filename){
        return tree.containsKey(filename);
    }
    
    public String getFileSha(String filename){
        return tree.get(filename);
    }


    @Override
    public String toString() {
        if (parent == null){
            return "Commit{" + " Initialised jit ;) repository" +
                    ", message='" + message + '\'' +
                    ", date=" + date +
                    '}';
        }
        String sha1ofparent = sha1((Object) serialize(parent));
        return "Commit{" +
                "parent=" +  sha1ofparent +
                ", message='" + message + '\'' +
                ", date=" + date +
                '}';
    }
}
