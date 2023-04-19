package telran.git.controller;

import telran.git.GitRepository;
import telran.view.InputOutput;
import telran.view.Item;

public class GitItems {
	private static GitRepository git;

	public static Item[] getGitItems(GitRepository gitRepository) {
		GitItems.git = gitRepository;
		Item[] items = { Item.of("Commit", GitItems::commit), Item.of("Get Info", GitItems::info),
				Item.of("Commit content", GitItems::commitContent), Item.of("Create branch", GitItems::createBranch),
				Item.of("Rename branch", GitItems::renameBranch), Item.of("Delete branch", GitItems::deleteBranch),
				Item.of("Show branches", GitItems::branches), Item.of("log", GitItems::log),
				Item.of("Get Head", GitItems::getHead), Item.of("Switch to", GitItems::switchTo),
				Item.of("Add regex to ignore", GitItems::addRegex), Item.of("Exit", io -> gitRepository.save(), true) };
		return items;
	}

	private static void commit(InputOutput io) {
		String commitMessage = io.readString("Enter commit message");
		io.writeLine(git.commit(commitMessage));
	}

	private static void info(InputOutput io) {
		git.info().forEach(io::writeLine);
	}

	private static void createBranch(InputOutput io) {
		String branchName = enterBranchName(io, "branch name");
		io.writeLine(git.createBranch(branchName));
	}

	private static String enterBranchName(InputOutput io, String prompt) {
		return io.readStringPredicate("Enter " + prompt, "Wrong name of branch", t -> t.matches("\\w{3,}"));
	}

	private static void renameBranch(InputOutput io) {
		String oldBranchName = enterBranchName(io, "Old branch name");
		if (git.branches().stream().anyMatch(n -> n.contains(oldBranchName))) {
			String newBranchName = enterBranchName(io, "New branch name");
			io.writeLine(git.renameBranch(oldBranchName, newBranchName));
		} else {
			io.writeLine("branch doesn't exists");
		}
	}

	private static void deleteBranch(InputOutput io) {
		String branchName = enterBranchName(io, "branch name");
		if (git.branches().stream().anyMatch(n -> n.contains(branchName))) {
			io.writeLine(git.deleteBranch(branchName));
		} else {
			io.writeLine("branch doesn't exists");
		}
	}

	private static void log(InputOutput io) {
		git.log().forEach(io::writeLine);
	}

	private static void branches(InputOutput io) {
		git.branches().forEach(io::writeLine);
	}

	private static void commitContent(InputOutput io) {
		String commitName = io.readString("Enter commit name");
		io.writeLine(git.commitContent(commitName));
	}

	private static void switchTo(InputOutput io) {
		String commitOrBranchName = io.readString("Enter commit or branch name for switching");
		io.writeLine(git.switchTo(commitOrBranchName));
	}

	private static void getHead(InputOutput io) {
		String head = git.getHead();
		io.writeLine(head != null ? head : "Head on commit");
	}

	private static void addRegex(InputOutput io) {
		String regex = io.readStringPredicate("Enter regex", "Wrong regex", s -> {
			boolean res = true;
			try {
				"test".matches(s);
			} catch (Exception e) {
				res = false;
			}
			return res;
		});
		io.writeLine(git.addIgnoredFileNameExp(regex));
	}
}
