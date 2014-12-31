package com.nc.gs.tests;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

public class StopWatch {

	/**
	 * Inner class to hold data about one task executed within the stop watch.
	 */
	public static final class TaskInfo implements Comparable<TaskInfo> {

		private final String taskName;

		private final long timeMillis;

		TaskInfo(String taskName, long timeMillis) {
			this.taskName = taskName;
			this.timeMillis = timeMillis;
		}

		@Override
		public int compareTo(TaskInfo o) {
			int rv;
			if (o == this) {
				rv = 0;
			} else {

				if ((rv = Long.compare(timeMillis, o.timeMillis)) == 0) {
					rv = taskName.compareTo(o.taskName);
				}
			}
			return rv;
		}

		/**
		 * Return the name of this task.
		 */
		public String getTaskName() {
			return this.taskName;
		}

		/**
		 * Return the time in milliseconds this task took.
		 */
		public long getTimeMillis() {
			return this.timeMillis;
		}

		/**
		 * Return the time in seconds this task took.
		 */
		public double getTimeSeconds() {
			return this.timeMillis / 1000.0;
		}

		public double pctVs(TaskInfo other) {
			double t = timeMillis;

			double ot = other.timeMillis;

			return 1d - ot / t;
		}

		public String reportPctVs(TaskInfo other) {
			double pct = pctVs(other);

			if (pct < 0) {
				return String.format("%s was %.2fx [%.2f%%] faster than %s", taskName, pctToMultiple(pct), -pct * 100, other.taskName);
			} else if (pct > 0) {
				return String.format("%s was %.2fx [%.2f%%] slower than %s", taskName, pctToMultiple(pct), pct * 100, other.taskName);
			} else {
				return String.format("%s executed as fast as %s", taskName, other.taskName);
			}
		}

		@Override
		public String toString() {
			return String.format("[task:%s, time:%d(ms)]", taskName, timeMillis);
		}

	}

	static final GarbageCollectorMXBean[] GCS;

	static {
		List<GarbageCollectorMXBean> beans = ManagementFactory.getGarbageCollectorMXBeans();

		GCS = beans.toArray(new GarbageCollectorMXBean[beans.size()]);
	}

	public static long gcOverheadMillis() {
		long l = 0;

		for (GarbageCollectorMXBean gc : GCS) {
			long t = gc.getCollectionTime();
			if (t > 0) {
				l += t;
			}
		}

		return l;
	}

	static double pctToMultiple(double pct) {
		return pct > 0 ? 1 / (1 - pct) : pct < 0 ? 1 - pct : 0;
	}

	static double pctVs(long l, long r) {
		double t = l;

		double ot = r;

		return 1d - ot / t;
	}

	/**
	 * Identifier of this stop watch. Handy when we have output from multiple stop watches and need
	 * to distinguish between them in log or console output.
	 */
	private final String id;

	private boolean keepTaskList = true;

	private final List<TaskInfo> taskList = new LinkedList<TaskInfo>();

	/** Start time of the current task */
	private long startTimeMillis;

	private long gcMillisBefore;

	/** Is the stop watch currently running? */
	private boolean running;

	/** Name of the current task */
	private String currentTaskName;

	private TaskInfo lastTaskInfo;

	private int taskCount;

	/** Total running time */
	private long totalTimeMillis;

	/**
	 * Construct a new stop watch. Does not start any task.
	 */
	public StopWatch() {
		this.id = "";
	}

	/**
	 * Construct a new stop watch with the given id. Does not start any task.
	 * 
	 * @param id
	 *            identifier for this stop watch. Handy when we have output from multiple stop
	 *            watches and need to distinguish between them.
	 */
	public StopWatch(String id) {
		this.id = id;
	}

	public String compareFastest(StopWatch other) {
		return fastestTask().reportPctVs(other.fastestTask());
	}

	public TaskInfo fastestTask() {
		return orderedTasks(true).first();
	}

	/**
	 * Return the last task as a TaskInfo object.
	 */
	public TaskInfo getLastTaskInfo() throws IllegalStateException {
		if (this.lastTaskInfo == null) {
			throw new IllegalStateException("No tasks run: can't get last task info");
		}
		return this.lastTaskInfo;
	}

	/**
	 * Return the name of the last task.
	 */
	public String getLastTaskName() throws IllegalStateException {
		if (this.lastTaskInfo == null) {
			throw new IllegalStateException("No tasks run: can't get last task name");
		}
		return this.lastTaskInfo.getTaskName();
	}

	/**
	 * Return the time taken by the last task.
	 */
	public long getLastTaskTimeMillis() throws IllegalStateException {
		if (this.lastTaskInfo == null) {
			throw new IllegalStateException("No tasks run: can't get last task interval");
		}
		return this.lastTaskInfo.getTimeMillis();
	}

	/**
	 * Return the number of tasks timed.
	 */
	public int getTaskCount() {
		return this.taskCount;
	}

	/**
	 * Return an array of the data for tasks performed.
	 */
	public TaskInfo[] getTaskInfo() {
		guardGetTask();
		return this.taskList.toArray(new TaskInfo[this.taskList.size()]);
	}

	/**
	 * Return the total time in milliseconds for all tasks.
	 */
	public long getTotalTimeMillis() {
		return this.totalTimeMillis;
	}

	/**
	 * Return the total time in seconds for all tasks.
	 */
	public double getTotalTimeSeconds() {
		return this.totalTimeMillis / 1000.0;
	}

	private void guardGetTask() {
		if (!this.keepTaskList) {
			throw new UnsupportedOperationException("Task info is not being kept!");
		}
	}

	/**
	 * Return whether the stop watch is currently running.
	 */
	public boolean isRunning() {
		return this.running;
	}

	public NavigableSet<TaskInfo> orderedTasks(boolean asc) {
		guardGetTask();

		TreeSet<TaskInfo> set = new TreeSet<>(this.taskList);

		return asc ? set : set.descendingSet();
	}

	public double pctVs(StopWatch other, int maxDiscards) {
		double t = totalTimeDiscardingSlowest(maxDiscards);

		double ot = other.totalTimeDiscardingSlowest(maxDiscards);

		return 1d - ot / t;
	}

	/**
	 * Return a string with a table describing all tasks performed. For custom reporting, call
	 * getTaskInfo() and use the task info directly.
	 */
	public String prettyPrint() {
		guardGetTask();
		return print(this.taskList);
	}

	public String prettyPrintTimed(boolean asc) {
		return print(orderedTasks(asc));
	}

	private String print(Iterable<TaskInfo> tasks) {
		StringBuilder sb = new StringBuilder(shortSummary());
		sb.append('\n');
		if (!this.keepTaskList) {
			sb.append("No task info kept");
		} else {
			sb.append("-----------------------------------------\n");
			sb.append("ms     %     Task name\n");
			sb.append("-----------------------------------------\n");
			NumberFormat nf = NumberFormat.getNumberInstance();
			nf.setMinimumIntegerDigits(5);
			nf.setGroupingUsed(false);
			NumberFormat pf = NumberFormat.getPercentInstance();
			pf.setMinimumIntegerDigits(3);
			pf.setGroupingUsed(false);
			for (TaskInfo task : tasks) {
				sb.append(nf.format(task.getTimeMillis())).append("  ");
				sb.append(pf.format(task.getTimeSeconds() / getTotalTimeSeconds())).append("  ");
				sb.append(task.getTaskName()).append("\n");
			}
		}
		return sb.toString();
	}

	public String reportPctVs(StopWatch other, int maxDiscards) {
		long tElapsed = totalTimeDiscardingSlowest(maxDiscards);
		long oElapsed = other.totalTimeDiscardingSlowest(maxDiscards);

		double pct = pctVs(tElapsed, oElapsed);

		if (pct < 0) {
			return String.format("%s was %.2fx [%.2f%%] faster than %s. [%d<%d]", id, pctToMultiple(pct), -pct * 100, other.id, tElapsed, oElapsed);
		} else if (pct > 0) {
			return String.format("%s was %.2fx [%.2f%%] slower than %s. [%d>%d]", id, pctToMultiple(pct), pct * 100, other.id, tElapsed, oElapsed);
		} else {
			return String.format("%s executed as fast as %s. [%d=%d]", id, other.id, tElapsed, oElapsed);
		}
	}

	/**
	 * Determine whether the TaskInfo array is built over time. Set this to "false" when using a
	 * StopWatch for millions of intervals, or the task info structure will consume excessive
	 * memory. Default is "true".
	 */
	public void setKeepTaskList(boolean keepTaskList) {
		this.keepTaskList = keepTaskList;
	}

	/**
	 * Return a short description of the total running time.
	 */
	public String shortSummary() {
		return "StopWatch '" + this.id + "': running time (millis) = " + getTotalTimeMillis();
	}

	public TaskInfo slowestTask() {
		return orderedTasks(true).last();
	}

	/**
	 * Start an unnamed task. The results are undefined if {@link #stop()} or timing methods are
	 * called without invoking this method.
	 * 
	 * @see #stop()
	 */
	public void start() throws IllegalStateException {
		start("");
	}

	/**
	 * Start a named task. The results are undefined if {@link #stop()} or timing methods are called
	 * without invoking this method.
	 * 
	 * @param taskName
	 *            the name of the task to start
	 * @see #stop()
	 */
	public void start(String taskName) throws IllegalStateException {
		if (this.running) {
			throw new IllegalStateException("Can't start StopWatch: it's already running");
		}
		gcMillisBefore = gcOverheadMillis();
		this.startTimeMillis = System.currentTimeMillis();
		this.running = true;
		this.currentTaskName = taskName;
	}

	/**
	 * Stop the current task. The results are undefined if timing methods are called without
	 * invoking at least one pair {@link #start()} / {@link #stop()} methods.
	 * 
	 * @see #start()
	 */
	public void stop() throws IllegalStateException {
		if (!this.running) {
			throw new IllegalStateException("Can't stop StopWatch: it's not running");
		}
		long lastTime = System.currentTimeMillis() - this.startTimeMillis;

		lastTime -= (gcOverheadMillis() - gcMillisBefore);

		this.totalTimeMillis += lastTime;
		this.lastTaskInfo = new TaskInfo(this.currentTaskName, lastTime);
		if (this.keepTaskList) {
			this.taskList.add(lastTaskInfo);
		}
		++this.taskCount;
		this.running = false;
		this.currentTaskName = null;
	}

	public List<TaskInfo> tasks() {
		guardGetTask();

		return Collections.unmodifiableList(taskList);
	}

	/**
	 * Return an informative string describing all tasks performed For custom reporting, call
	 * <code>getTaskInfo()</code> and use the task info directly.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(shortSummary());
		if (this.keepTaskList) {
			for (TaskInfo task : getTaskInfo()) {
				sb.append("; [").append(task.getTaskName()).append("] took ").append(task.getTimeMillis());
				long percent = Math.round((100.0 * task.getTimeSeconds()) / getTotalTimeSeconds());
				sb.append(" = ").append(percent).append("%");
			}
		} else {
			sb.append("; no task info kept");
		}
		return sb.toString();
	}

	public long totalTimeDiscardingSlowest(int max) {
		NavigableSet<TaskInfo> tasks = orderedTasks(false);
		Iterator<TaskInfo> itr = tasks.iterator();

		int r = max;
		while (r > 0 && itr.hasNext()) {
			itr.next();

			if (itr.hasNext()) {
				itr.remove();
				r--;
			}
		}

		itr = tasks.iterator();

		long l = 0;

		while (itr.hasNext()) {
			l += itr.next().getTimeMillis();
		}

		return l;
	}
}
