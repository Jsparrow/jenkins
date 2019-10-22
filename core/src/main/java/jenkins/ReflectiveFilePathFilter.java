package jenkins;

import java.io.File;

/**
 * Convenient adapter for {@link FilePathFilter} that allows you to handle all
 * operations as a single string argument.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.587 / 1.580.1
 */
public abstract class ReflectiveFilePathFilter extends FilePathFilter {
    /**
     * @param name
     *      Name of the operation.
     */
    protected abstract boolean op(String name, File path);

    @Override
    public boolean read(File f) {
        return op("read", f);
    }

    @Override
    public boolean write(File f) {
        return op("write", f);
    }

    @Override
    public boolean symlink(File f) {
        return op("symlink",f);
    }

    @Override
    public boolean mkdirs(File f) {
        return op("mkdirs", f);
    }

    @Override
    public boolean create(File f) {
        return op("create", f);
    }

    @Override
    public boolean delete(File f) {
        return op("delete", f);
    }

    @Override
    public boolean stat(File f) {
        return op("stat", f);
    }
}
