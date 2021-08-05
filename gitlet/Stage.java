package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.*;

import static gitlet.Repository.*;
import static gitlet.Utils.*;

public class Stage implements Serializable {

    private TreeMap<String, String> addList;
    private List<String> removeList;

    public Stage(){
        addList = new TreeMap<>();
        removeList = new ArrayList<>();
    }

    public void stageForAddition (String filename, String value){
        addList.put(filename, value);
        removeList.remove(filename);
    }

    public void unstage (String filename){
        addList.remove(filename);
        removeList.add(filename);
    }

    // updates commit tree and clears staging area
    public TreeMap<String, String> updateTree(TreeMap<String, String> headTree){
        if (headTree == null){
            headTree = new TreeMap<>();
        }
        for (String key : addList.keySet()){
            headTree.put(key, addList.get(key));
        }
        for (String key : removeList){
            headTree.remove(key);
        }

        addList.clear();
        removeList.clear();

        writeObject(INDEX, this);

        return headTree;

    }

    //also deletes the files it tracks
    public void clearArea(){
        for (String filename : addList.keySet()){
            File f = join(CWD, filename);
            restrictedDelete(f);
        }
        addList.clear();
        removeList.clear();
    }


    public boolean isCleared(){
        return addList.isEmpty() && removeList.isEmpty();
    }

    public String toString(){
        return "{Addition : " + addList.toString() + " " + "Removal : " + removeList.toString() + "}";
    }

    public void printStatus() {
        System.out.println("=== Staged Files ===");
        printList(addList.keySet());
        System.out.println("=== Removed Files ===");
        printList(removeList);
    }

    private void printList(Collection<String> list){
        for (String name : list){
            System.out.println(name);
        }
    }
}