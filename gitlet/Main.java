package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import static gitlet.Repository.HEAD;
import static gitlet.Repository.INDEX;
import static gitlet.Utils.*;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        // TODO: what if args is empty?
        String firstArg = args[0];

        try {
            Repository.setupPersistence();
        } catch (IOException e) {
            System.out.println("Error while setting up gitlet repo");
            e.printStackTrace();
        }
        switch(firstArg) {
            case "init":
                Repository.createInitialCommit();
                break;
            case "add":
                String fileToBeAdded = args[1];
                Repository.add(fileToBeAdded);
                break;
            case "commit":
                String message = args[1];
                Repository.commitCurrentState(message);
                break;
            case "rm":
                String fileToBeRemoved = args[1];
                Repository.remove(fileToBeRemoved);
                break;
            case "log":
                // gets the history of current head
                Repository.getLog();
                break;
            case "global-log":
                Repository.getGlobalLog();
                break;
            case "find":
                String query = args[1];
                Repository.find(query);
                break;
            case "status":
                Repository.status();
                break;
            case "checkout":
                if (args.length == 2){
                    String branchName = args[1];
                    Repository.checkoutBranch(branchName);
                }else if (args.length == 3){
                    String fileName = args[2];
                    Repository.checkoutFile(fileName);
                }else if (args.length == 4){
                    String commitId = args[1];
                    String fileName = args[3];
                    Repository.checkoutFile(commitId, fileName);
                }else {
                    System.out.println("Error : Checkout command help!");
                }
                break;
            case "branch":
                String branchName = args[1];
                Repository.createNewBranch(branchName);
                break;
            case "rm-branch":
                String branchToBeDeleted = args[1];
                System.out.println(branchToBeDeleted);
                Repository.deleteBranch(branchToBeDeleted);
                break;
            case "reset":
                String commitId = args[1];
                Repository.reset(commitId);
                break;
            case "merge":
                String givenBranch = args[1];
                Repository.merge(givenBranch);
                break;
        }
    }
}
