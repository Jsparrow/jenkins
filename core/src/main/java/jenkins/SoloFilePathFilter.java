package jenkins;

import hudson.FilePath;

import javax.annotation.Nullable;
import java.io.File;

/**
 * Variant of {@link FilePathFilter} that assumes it is the sole actor.
 *
 * It throws {@link SecurityException} instead of returning false. This makes it the
 * convenient final wrapper for the caller.
 *
 * @author Kohsuke Kawaguchi
 */
public final class SoloFilePathFilter extends FilePathFilter {
    private final FilePathFilter base;

    private SoloFilePathFilter(FilePathFilter base) {
        this.base = base;
    }

    /**
     * Null-safe constructor.
     */
    public static @Nullable SoloFilePathFilter wrap(@Nullable FilePathFilter base) {
        if (base==null) {
			return null;
		}
        return new SoloFilePathFilter(base);
    }

    private boolean noFalse(String op, File f, boolean b) {
        if (!b) {
			throw new SecurityException(new StringBuilder().append("agent may not ").append(op).append(" ").append(f).append("\nSee https://jenkins.io/redirect/security-144 for more details").toString());
		}
        return true;
    }
    
    private File normalize(File file){
        return new File(FilePath.normalize(file.getAbsolutePath()));
    }

    @Override
    public boolean read(File f) {
        return noFalse("read",f,base.read(normalize(f)));
    }

    @Override
    public boolean write(File f) {
        return noFalse("write",f,base.write(normalize(f)));
    }

    @Override
    public boolean symlink(File f) {
        return noFalse("symlink",f,base.write(normalize(f)));
    }

    @Override
    public boolean mkdirs(File f) {
        return noFalse("mkdirs",f,base.mkdirs(normalize(f)));
    }

    @Override
    public boolean create(File f) {
        return noFalse("create",f,base.create(normalize(f)));
    }

    @Override
    public boolean delete(File f) {
        return noFalse("delete",f,base.delete(normalize(f)));
    }

    @Override
    public boolean stat(File f) {
        return noFalse("stat",f,base.stat(normalize(f)));
    }
}
