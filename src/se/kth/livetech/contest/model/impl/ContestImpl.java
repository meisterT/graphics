package se.kth.livetech.contest.model.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import se.kth.livetech.contest.model.Attrs;
import se.kth.livetech.contest.model.Clar;
import se.kth.livetech.contest.model.Contest;
import se.kth.livetech.contest.model.Finalized;
import se.kth.livetech.contest.model.Info;
import se.kth.livetech.contest.model.Judgement;
import se.kth.livetech.contest.model.Language;
import se.kth.livetech.contest.model.Problem;
import se.kth.livetech.contest.model.Region;
import se.kth.livetech.contest.model.Reset;
import se.kth.livetech.contest.model.Run;
import se.kth.livetech.contest.model.Team;
import se.kth.livetech.contest.model.TeamScore;
import se.kth.livetech.contest.model.Testcase;
import se.kth.livetech.util.DebugTrace;

public class ContestImpl implements Contest {
	Info info;
	Finalized finalized;
	Map<Integer, Team> teams;
	Map<Integer, Region> regions;
	Map<Integer, Problem> problems;
	Map<String, Language> languages;
	Map<String, Judgement> judgements;
	Map<Integer, Run> runs;
	Map<Integer, Clar> clars;
	Map<Integer, Map<Integer, List<Integer>>> runtable;
	Map<Integer, TeamScore> scores;
	Map<Integer, Map<Integer,Testcase>> testcases;
	List<Integer> ranking;
	private TeamCompScore teamCompScore;
	private TeamCompAlpha teamCompAlpha;
	Map<Integer, Integer> teamRows;

	private class RunComp implements Comparator<Integer> {
		public int compare(Integer a, Integer b) {
			int ta = runs.get(a).getTime();
			int tb = runs.get(b).getTime();
			return ta != tb ? ta - tb : a - b;
		}
	}

	private class TeamCompScore implements Comparator<Integer> {
		public int compare(Integer a, Integer b) {
			return -getTeamScore(a).compareTo(getTeamScore(b));
		}
	}

	private class TeamCompAlpha implements Comparator<Integer> {
		public int compare(Integer a, Integer b) {
			int ds = -getTeamScore(a).compareTo(getTeamScore(b));
			if (ds != 0)
				return ds;
			Team teamA = getTeam(a);
			Team teamB = getTeam(b);
			if(teamA != null && teamB != null) {
				return getTeam(a).getName().compareTo(getTeam(b).getName());
			}
			return teamA != null ? 1 : teamB != null ? -1 : 0;
		}
	}

	public ContestImpl() {
		reset();
	}

	public void reset() {
		info = new InfoImpl(new TreeMap<String, String>());
		finalized = new FinalizedImpl(new TreeMap<String, String>());
		teams = new TreeMap<Integer, Team>();
		regions = new TreeMap<Integer, Region>();
		problems = new TreeMap<Integer, Problem>();
		languages = new TreeMap<String, Language>();
		judgements = new TreeMap<String, Judgement>();
		runs = new TreeMap<Integer, Run>();
		clars = new TreeMap<Integer, Clar>();
		runtable = new TreeMap<Integer, Map<Integer, List<Integer>>>();
		scores = new TreeMap<Integer, TeamScore>();
		ranking = new ArrayList<Integer>();
		teamCompScore = new TeamCompScore();
		teamCompAlpha = new TeamCompAlpha();
		teamRows = new TreeMap<Integer, Integer>();
		testcases = new TreeMap<Integer, Map<Integer,Testcase>>();
	}

	public ContestImpl(ContestImpl old, Attrs a) {
		info = old.info;
		finalized = old.finalized;
		teams = old.teams;
		regions = old.regions;
		problems = old.problems;
		languages = old.languages;
		judgements = old.judgements;
		runs = old.runs;
		clars = old.clars;
		runtable = old.runtable;
		scores = old.scores;
		ranking = old.ranking;
		teamCompScore = new TeamCompScore();
		teamCompAlpha = new TeamCompAlpha();
		teamRows = old.teamRows;
		testcases = old.testcases;
		update(a);
	}

	public Info getInfo() {
		return info;
	}
	
	public Finalized getFinalized() {
		return finalized;
	}

	public Set<Integer> getTeams() {
		return teams.keySet();
	}

	public Team getTeam(int i) {
		return teams.get(i);
	}
	
	public Set<Integer> getRegions() {
		return regions.keySet();
	}
	
	public Region getRegion(int i) {
		return regions.get(i);
	}

	public Set<Integer> getProblems() {
		return problems.keySet();
	}

	public Problem getProblem(int i) {
		return problems.get(i);
	}

	public Set<String> getLanguages() {
		return languages.keySet();
	}

	public Language getLanguage(String i) {
		return languages.get(i);
	}

	public Set<String> getJudgements() {
		return judgements.keySet();
	}

	public Judgement getJudgement(String i) {
		return judgements.get(i);
	}

	public Set<Integer> getRuns() {
		return runs.keySet();
	}

	public Run getRun(int i) {
		return runs.get(i);
	}
	
	public Testcase getTestcase(int run, int i) {
		if(testcases.containsKey(run))
			return testcases.get(run).get(i);
		else return null;
	}
	
	public Map<Integer, Testcase> getTestcases(int run) {
		return testcases.get(run);
	}

	public Set<Integer> getClars() {
		return clars.keySet();
	}

	public Clar getClar(int i) {
		return clars.get(i);
	}

	private List<Integer> getRunList(int team, int problem) {
		if (!runtable.containsKey(team))
			runtable.put(team, new TreeMap<Integer, List<Integer>>());
		if (!runtable.get(team).containsKey(problem))
			runtable.get(team).put(problem, new ArrayList<Integer>());
		return runtable.get(team).get(problem);
	}

	public int getRuns(int team, int problem) {
		return getRunList(team, problem).size();
	}

	public Run getRun(int team, int problem, int i) {
		return getRun(getRunList(team, problem).get(i));
	}

	public TeamScore getTeamScore(int team) {
		if (!scores.containsKey(team) && teams.containsKey(team))
			scores.put(team, new TeamScoreImpl(this, team));
		return scores.get(team);
	}

	public int getTeamRank(int team) {
		for (int i = 0; i < ranking.size(); ++i)
			if (teamCompScore.compare(team, ranking.get(i)) <= 0)
				return i + 1;
		return ranking.size() + 1;
		/*
		 * int index = Collections.binarySearch(ranking, team, teamComp); if
		 * (index < 0) index = -index - 1; while (index > 0 && ranking.get(index
		 * - 1).compareTo(team) == 0) --index; return index + 1;
		 */
	}

	public Team getRankedTeam(int rank) {
		return teams.get(ranking.get(rank - 1));
	}

	public int getTeamRow(int team) {
		return teamRows.get(team);
	}

	private <T> List<T> relist(List<T> list, T t, Comparator<T> c) {
		List<T> relist = new ArrayList<T>(list);
		if (!relist.contains(t))
			relist.add(t);
		Collections.sort(relist, c);
		return relist;
	}

	private Map<Integer, Integer> rerow(List<Integer> list) {
		Map<Integer, Integer> rows = new TreeMap<Integer, Integer>();
		int row = 0;
		for (int item : list)
			rows.put(item, row++);
		return rows;
	}

	private <K, V> Map<K, V> remap(Map<K, V> map, K k, V v) {
		Map<K, V> remap = new TreeMap<K, V>(map);
		remap.put(k, v);
		return remap;
	}

	private void update(Attrs a) {
		//DebugTrace.trace("update " + a);
		if (a instanceof Reset) {
			reset();
		}
		else if (a instanceof Run) {
			Run r = (Run) a;
			int i = r.getId(), t = r.getTeam(), p = r.getProblem();
			if (!this.teams.containsKey(t)) {
				DebugTrace.trace();
				return;
			}
			runs = remap(runs, i, r);
			// Update sorted list of runs
			List<Integer> s = relist(getRunList(t, p), i, new RunComp());
			Map<Integer, List<Integer>> m = remap(runtable.get(t), p, s);
			runtable = remap(runtable, t, m);
			// Update scores and ranking
			scores = remap(scores, t, new TeamScoreImpl(this, t));
			ranking = relist(ranking, t, teamCompAlpha);
			teamRows = rerow(ranking);
		} else if(a instanceof Testcase) {
			Testcase t = (Testcase) a;
			if (!runs.containsKey(t.getRunId())){
				DebugTrace.trace();
				return;
			}
			Map<Integer, Testcase> runTestcases = testcases.get(t.getRunId());
			if(runTestcases==null) {
				runTestcases = new TreeMap<Integer, Testcase>();
				runTestcases.put(t.getI(), t);
			} else {
				runTestcases = remap(runTestcases, t.getI(), t);
			}
			testcases = remap(testcases, t.getRunId(), runTestcases);
			Run r = getRun(t.getRunId());
			/*if(r.getRunJudgement().isJudged()) { // Mark run as unjudged.
				Map<String, String> attrs = new LinkedHashMap<String, String>();
				for (String name : r.getProperties())
					attrs.put(name, r.getProperty(name));
				attrs.put("judged", "False");
				update(new RunImpl(attrs));
			}*/
		} else if (a instanceof Clar) {
			Clar c = (Clar) a;
			clars = remap(clars, c.getId(), c);
		} else if (a instanceof Problem) {
			Problem p = (Problem) a;
			problems = remap(problems, p.getId(), p);
		} else if (a instanceof Language) {
			Language l = (Language) a;
			languages = remap(languages, l.getName(), l);
		} else if (a instanceof Team) {
			Team t = (Team) a;
			teams = remap(teams, t.getId(), t);
			// Update ranking to include new team
			ranking = relist(ranking, t.getId(), teamCompAlpha);
			teamRows = rerow(ranking);
		} else if (a instanceof Region) {
			Region r = (Region) a;
			regions = remap(regions, r.getId(), r);
		} else if (a instanceof Judgement) {
			Judgement j = (Judgement) a;
			judgements = remap(judgements, j.getAcronym(), j);
		} else if (a instanceof Info) {
			info = (Info) a;
		} else if(a instanceof Finalized) {
			finalized = (Finalized) a;
		} else {
			new Error("Unknown Attrs type " + a.getClass()).printStackTrace();
		}
	}
}
