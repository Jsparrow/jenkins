package jenkins;

import hudson.Extension;
import hudson.FilePath;
import hudson.remoting.ChannelProperty;

import java.io.File;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Maintains a bundle of {@link FilePathFilter} and implement a hook that broadcasts to all the filters.
 *
 * Accessible as channel property.
 *
 * @author Kohsuke Kawaguchi
 * @see FilePath
 * @since 1.587 / 1.580.1
 */
class FilePathFilterAggregator extends FilePathFilter {
    static final ChannelProperty<FilePathFilterAggregator> KEY = new ChannelProperty<>(FilePathFilterAggregator.class, "FilePathFilters");

	public static final int DEFAULT_ORDINAL = 0;

	private final CopyOnWriteArrayList<Entry> all = new CopyOnWriteArrayList<>();

	public final void add(FilePathFilter f) {
        add(f, DEFAULT_ORDINAL);
    }

	/**
     *
     * @param ordinal
     *      Crude ordering control among {@link FilePathFilter} ala {@link Extension#ordinal()}.
     *      A filter with a bigger value will get precedence. Defaults to 0.
     */
    public void add(FilePathFilter f, double ordinal) {
        Entry e = new Entry(f, ordinal);
        int i = Collections.binarySearch(all, e, Collections.reverseOrder());
        if (i>=0) {
			all.add(i,e);
		} else {
			all.add(-i-1,e);
		}
    }

	public void remove(FilePathFilter f) {
        all.stream().filter(e -> e.filter==f).forEach(all::remove);
    }

	/**
     * If no filter cares, what to do?
     */
    protected boolean defaultAction() {
        return false;
    }

	@Override
    public boolean read(File f) {
        return all.stream().filter(e -> e.filter.read(f)).findFirst().map(e -> true).orElse(defaultAction());
    }

	@Override
    public boolean mkdirs(File f) {
        return all.stream().filter(e -> e.filter.mkdirs(f)).findFirst().map(e -> true).orElse(defaultAction());
    }

	@Override
    public boolean write(File f) {
        return all.stream().filter(e -> e.filter.write(f)).findFirst().map(e -> true).orElse(defaultAction());
    }

	@Override
    public boolean symlink(File f) {
        return all.stream().filter(e -> e.filter.symlink(f)).findFirst().map(e -> true).orElse(defaultAction());
    }

	@Override
    public boolean create(File f) {
        return all.stream().filter(e -> e.filter.create(f)).findFirst().map(e -> true).orElse(defaultAction());
    }

	@Override
    public boolean delete(File f) {
        return all.stream().filter(e -> e.filter.delete(f)).findFirst().map(e -> true).orElse(defaultAction());
    }

	@Override
    public boolean stat(File f) {
        return all.stream().filter(e -> e.filter.stat(f)).findFirst().map(e -> true).orElse(defaultAction());
    }

	@Override public String toString() {
        return "FilePathFilterAggregator" + all;
    }

	private class Entry implements Comparable<Entry> {
        final FilePathFilter filter;
        final double ordinal;

        private Entry(FilePathFilter filter, double ordinal) {
            this.filter = filter;
            this.ordinal = ordinal;
        }

        @Override
        public int compareTo(Entry that) {
            double result = Double.compare(this.ordinal, that.ordinal);

            if (result < 0) {
				return -1;
			}
            if (result > 0) {
				return 1;
			}

            // to create predictable order that doesn't depend on the insertion order, use class name
            // to break a tie
            return this.filter.getClass().getName().compareTo(that.filter.getClass().getName());
        }
    }
}
