package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author gatsby003
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));

    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, "/.gitlet");

    // .gitlet subdirectories 
    static final File HEAD = join(CWD, "/.gitlet/HEAD");
    static final File INDEX = join(CWD, "/.gitlet/INDEX");
    
    static final File OBJECTS = join(CWD, "/.gitlet/objects");
    static final File BLOBS = join(CWD, "/.gitlet/objects/blobs");
    static final File COMMITS = join(CWD, "/.gitlet/objects/commits");
    static final File BRANCHES = join(CWD, "/.gitlet/branches");
    static final File LOGS = join(CWD, "/.gitlet/logs");

    static Stage stagingArea = null;
    static String head = "";


    public static void setupPersistence() throws IOException {
        if (!GITLET_DIR.exists()){
            GITLET_DIR.mkdir();
        }
        if (!OBJECTS.exists()){
            OBJECTS.mkdir();
        }
        if (!BLOBS.exists()){
            BLOBS.mkdir();
        }
        if (!COMMITS.exists()){
            COMMITS.mkdir();
        }
        if (!BRANCHES.exists()){
            BRANCHES.mkdir();
        }
        if (!LOGS.exists()){
            LOGS.mkdir();
        }

        if (HEAD.createNewFile()){
            writeObject(HEAD, head);
        }else {
            head = readObject(HEAD, String.class);
        }

        if (INDEX.createNewFile()){
            stagingArea = new Stage();
            writeObject(INDEX,stagingArea);
        }else {
            stagingArea = readObject(INDEX, Stage.class);
        }

    }

    public static void commitCurrentState(String message) {
        if (stagingArea.isCleared()){
            System.out.println("Nothing to Commit");
            return;
        }
        try {
            Commit newCommit = new Commit(message);
            newCommit.saveCommit();
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }

    }

    public static void createInitialCommit() {
        if (head.equals("")){
            Commit initialCommit = new Commit("Initial Commit");
            createNewBranch("master");
            initialCommit.saveCommit();
        }else {
            System.out.println("Git repo already initialised");
        }

    }

    public static void createNewBranch(String name){
        // check if branch exists
        List<String> l = plainFilenamesIn(join(CWD, ".gitlet/branches"));
        if (l != null && l.contains(name)){
            System.out.println("Branch already exists!");
            return;
        }

        // go in the branches folder and create a file
        File branch = join(CWD, ".gitlet/branches", name);
        try {
            branch.createNewFile();
            if (head.equals("")){
                writeObject(branch, "");
                writeObject(HEAD, name);
            }else {
                String sha = readObject(join(CWD, ".gitlet/branches", head), String.class);
                writeObject(branch, sha);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void add(String filename){
        // todo : check actual blob to verify file edits
        File file = join(CWD, filename);
        if (file.exists()){
            byte[] filecontent = readContents(file);
            String value = sha1(filecontent, filename);
            if (persistFile(file, value)){
                stagingArea.stageForAddition(filename, value);
                writeObject(INDEX, stagingArea);
            }
        }
    }

    public static void remove(String filename){
        File file = join(CWD, filename);
        if (restrictedDelete(file)){
            stagingArea.unstage(filename);
            writeObject(INDEX, stagingArea);
        }else {
            System.out.println("Could not Delete File , wrong name ?");
        }
    }

    // returns true if new blob is created , false if blob already existed or if there was some error
    private static boolean persistFile(File file, String filename){
        File fileToBePersisted = join(CWD, "/.gitlet/objects/blobs" , filename);

        if (fileToBePersisted.exists()){
            System.out.println("Blob exists already");
            return false;
        }

        try {
            byte[] blobContent = readContents(file);
            fileToBePersisted.createNewFile();
            writeContents(fileToBePersisted, blobContent);
            return true;
        }catch (IOException e){
            System.out.println("error while creating commit object.");
            return false;
        }
    }

    public static void find(String message){
        List<String> a = plainFilenamesIn(join(CWD, ".gitlet/objects/commits"));

        assert a != null;
        Commit c;

        for (String commitName : a){
            c = Commit.getCommit(commitName);
            if (message.equals(c.message)){
                System.out.println(sha1((Object) serialize(c)));
            }
        }
    }

    public static void status(){
        List<String> branches = plainFilenamesIn(join(CWD, ".gitlet/branches"));
        // find current branch
        assert branches != null;
        System.out.println("=== Branches ===");
        System.out.println("*"+head);
        for (String branch : branches){
            if (branch.equals(head)){
                continue;
            }
            System.out.println(branch);
        }
        stagingArea.printStatus();
    }

    // log functions
    public static void getLog(){
        String commitId = readObject(join(CWD, ".gitlet/branches", head), String.class);
        Commit curr = Commit.getCommit(commitId);
        while (curr != null){
            printSingleCommit(curr);
            curr = Commit.getCommit(curr.parent);
        }
    }

    public static void getGlobalLog(){
        List<String> a = plainFilenamesIn(join(CWD, ".gitlet/objects/commits"));
        assert a != null;
        Commit c;
        for (String commitName : a){
            c = Commit.getCommit(commitName);
            printSingleCommit(c);
        }
    }

    private static void printSingleCommit(Commit c){
        System.out.println("\n" + "Commit: " + sha1((Object) serialize(c)) + "\n" +
                        "Date: " + c.getDate() + "\n" +
                         c.getMessage()
                );
    }

    public static void checkoutBranch(String branchName) {
        try {
            String chk = readObject(join(CWD, ".gitlet/branches", branchName), String.class);
            checkOutWithCommitId(chk);
            head = branchName;
            writeObject(HEAD, head);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void checkOutWithCommitId(String commitId){
        String curr = readObject(join(CWD, ".gitlet/branches", head), String.class);
        TreeMap<String, String> checkOutBranch = Commit.getCommit(commitId).tree;
        TreeMap<String, String> currentBranch = Commit.getCommit(curr).tree;
        try {
            // restores files from the checkout branch , overwriting if necessary
            for (String filename : checkOutBranch.keySet()) {
                File f1 = join(CWD, ".gitlet/objects/blobs", checkOutBranch.get(filename));
                File f2 = join(CWD, filename);

                byte[] content = readContents(f1);
                f2.createNewFile();
                writeContents(f2, content);

                if (currentBranch.containsKey(filename)) {
                    currentBranch.remove(filename);
                }
            }

            // deletes extra files tracked by currentBranch but not by checkOutBranch
            for (String filename : currentBranch.keySet()) {
                File f = join(CWD, filename);
                restrictedDelete(f);
            }
            // clears tracking lists and also deletes files that were added to be staged
            stagingArea.clearArea();

        }catch(IllegalArgumentException | IOException e){
            e.printStackTrace();
            System.out.println("Error while checking out branch");
        }
    }


    private static void restoreFile(Commit c, String fileName){
        if (!c.hasFile(fileName)){
            System.out.println("File does not exist.");
            return;
        }
        String fileSha = c.getFileSha(fileName);
        // getting the file blob
        File file = join(CWD, ".gitlet/objects/blobs", fileSha);
        byte[] contents = readContents(file);
        //getting the file from CWD
        File cwdFile = join(CWD, fileName);
        try {
            cwdFile.createNewFile();
            writeContents(cwdFile, contents);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void checkoutFile(String fileName) {
        Commit curr = readObject(join(CWD, ".gitlet/objects/commits", getBranch(head)), Commit.class);
        restoreFile(curr, fileName);
    }

    public static void checkoutFile(String commitId, String fileName) {
        Commit curr = readObject(join(CWD, ".gitlet/objects/commits", commitId), Commit.class);
        restoreFile(curr, fileName);
    }

    public static void deleteBranch(String branchToBeDeleted){
        File f = join(CWD, ".gitlet/branches", branchToBeDeleted);
        if (f.exists()){
            f.delete();
        }else {
            System.out.println("Branch does not exist!");
        }
    }

    public static void reset(String commitId){
        File commit = join(CWD, ".gitlet/objects", commitId);
        try {
            checkOutWithCommitId(commitId);
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    private static String getBranch(String name){
        try {
            return readObject(join(CWD, ".gitlet/branches", name), String.class);
        } catch(IllegalArgumentException e) {
            return null;
        }
    }

    private static List<String> getAncestors(String commitSha){
        List<String> l = new ArrayList<>();
        Commit curr = Commit.getCommit(commitSha);
        while (curr != null){
            l.add(curr.parent);
            curr = Commit.getCommit(curr.parent);
        }
        return l;
    }

    private static String findLatestAncestor(String curr, String given){
        List<String> currList = getAncestors(curr);
        List<String> givenList = getAncestors(given);
        String common = "";
        for (String sha : currList){
            if (givenList.contains(sha)){
                common = sha;
                break;
            }
        }
        return common;

    }

    public static void merge(String givenBranch){
        String curr = getBranch(head);
        String given = getBranch(givenBranch);
        if (given == null){
            System.out.println("Branch does not exist!");
            return;
        }
        // found latest common ancestor!
        String commonAncestor = findLatestAncestor(curr, given);

        if (commonAncestor.equals(curr)){
            System.out.println("Nothing to merge");
            return;
        }
        if (curr.equals(given)){
            System.out.println("Cannot merge with itself");
            return;
        }
        // Load all commits
        Commit currbranch = Commit.getCommit(curr);
        Commit givenbranch = Commit.getCommit(given);
        Commit ancestorCommit = Commit.getCommit(commonAncestor);

        // comparison tym find better option than tree.equals()
        if (ancestorCommit.tree.equals(givenbranch.tree)){
            System.out.println("Merge Complete!");
        }
        else if (ancestorCommit.tree.equals(currbranch.tree)){
            System.out.println("Branch brought up to date!");
            checkOutWithCommitId(given);
        }
        else {
            // check for rules
            for (String file : ancestorCommit.tree.keySet()){
                if (currbranch.tree.containsKey(file) && givenbranch.tree.containsKey(file)){
                    if (currbranch.tree.get(file).equals(ancestorCommit.tree.get(file))){
                        if (givenbranch.tree.get(file).equals(ancestorCommit.tree.get(file))){
                            continue;
                        }else {
                            checkoutFile(given, file);
                            add(file);
                        }
                    }else if (currbranch.tree.get(file).equals(givenbranch.tree.get(file))){
                        continue;
                    }else {
                        continue;
                    }
                }else if (currbranch.tree.containsKey(file) && !ancestorCommit.tree.containsKey(file)){
                    continue;
                }else if (givenbranch.tree.containsKey(file) && !ancestorCommit.tree.containsKey(file)){
                    checkoutFile(given, file);
                    add(file);
                }else if (currbranch.tree.get(file).equals(ancestorCommit.tree.get(file)) && !givenbranch.tree.containsKey(file)){
                    remove(file);
                }else if (!currbranch.tree.get(file).equals(givenbranch.tree.get(file))){
                    //generate merge conflict
                    System.out.println(
                            "<<<<<<<<<< HEAD \n" +
                            "content of file in current branch\n" +
                            readContentsAsString(join(CWD, file)) +
                            "\n" +
                            "content of file in given branch" +
                            ">>>>>>>>>>>\n" +
                            readContentsAsString(join(CWD, file))
                    );
                    return;
                }
                else {
                    // error checking
                    commitCurrentState("Merge Successful");
                    return;
                }
            }
        }
    }
}
