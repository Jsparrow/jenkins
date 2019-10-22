/*
 * The MIT License
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.os.solaris;

import jenkins.MasterToSlaveFileCallable;
import hudson.FileSystemProvisioner;
import hudson.FilePath;
import hudson.WorkspaceSnapshot;
import hudson.FileSystemProvisionerDescriptor;
import hudson.Extension;
import hudson.remoting.VirtualChannel;
import hudson.model.AbstractBuild;
import hudson.model.TaskListener;
import hudson.model.AbstractProject;
import hudson.model.Node;

import java.io.IOException;
import java.io.File;
import java.io.Serializable;

import org.jenkinsci.Symbol;
import org.jvnet.solaris.libzfs.LibZFS;
import org.jvnet.solaris.libzfs.ZFSFileSystem;

/**
 * {@link FileSystemProvisioner} for ZFS.
 *
 * @author Kohsuke Kawaguchi
 */
public class ZFSProvisioner extends FileSystemProvisioner implements Serializable {
    private static final LibZFS libzfs = new LibZFS();
	private static final long serialVersionUID = 1L;
	private final String rootDataset;

	public ZFSProvisioner(Node node) throws IOException, InterruptedException {
        rootDataset = node.getRootPath().act(new GetName());
    }

	@Override
	public void prepareWorkspace(AbstractBuild<?,?> build, FilePath ws, final TaskListener listener) throws IOException, InterruptedException {
        final String name = build.getProject().getFullName();
        
        ws.act(new PrepareWorkspace(name, listener));
    }

	@Override
	public void discardWorkspace(AbstractProject<?, ?> project, FilePath ws) throws IOException, InterruptedException {
        ws.act(new DiscardWorkspace());
    }

	/**
     * @deprecated as of 1.350
     */
    @Deprecated
    public WorkspaceSnapshot snapshot(AbstractBuild<?, ?> build, FilePath ws, TaskListener listener) throws IOException, InterruptedException {
        throw new UnsupportedOperationException();
    }

	@Override
	public WorkspaceSnapshot snapshot(AbstractBuild<?, ?> build, FilePath ws, String glob, TaskListener listener) throws IOException, InterruptedException {
        throw new UnsupportedOperationException();
    }
    private static class GetName extends MasterToSlaveFileCallable<String> {
            private static final long serialVersionUID = -2142349338699797436L;
            @Override
            public String invoke(File f, VirtualChannel channel) throws IOException {
                ZFSFileSystem fs = libzfs.getFileSystemByMountPoint(f);
                if(fs!=null) {
					return fs.getName();
				}

                // TODO: for now, only support agents that are already on ZFS.
                throw new IOException("Not on ZFS");
            }
    }

    private class PrepareWorkspace extends MasterToSlaveFileCallable<Void> {
        private static final long serialVersionUID = 2129531727963121198L;
		private final String name;
		private final TaskListener listener;
		PrepareWorkspace(String name, TaskListener listener) {
            this.name = name;
            this.listener = listener;
        }
		@Override
		public Void invoke(File f, VirtualChannel channel) throws IOException {
		    ZFSFileSystem fs = libzfs.getFileSystemByMountPoint(f);
		    if(fs!=null)
			 {
				return null;    // already on ZFS
			}

		    // nope. create a file system
		    String fullName = new StringBuilder().append(rootDataset).append('/').append(name).toString();
		    listener.getLogger().println(new StringBuilder().append("Creating a ZFS file system ").append(fullName).append(" at ").append(f).toString());
		    fs = libzfs.create(fullName, ZFSFileSystem.class);
		    fs.setMountPoint(f);
		    fs.mount();
		    return null;
		}
    }

    private static class DiscardWorkspace extends MasterToSlaveFileCallable<Void> {
            private static final long serialVersionUID = 1916618107019257530L;
            @Override
            public Void invoke(File f, VirtualChannel channel) throws IOException {
                ZFSFileSystem fs = libzfs.getFileSystemByMountPoint(f);
                if(fs!=null) {
					fs.destory(true);
				}
                return null;
            }
    }

    @Extension @Symbol("zfs")
    public static final class DescriptorImpl extends FileSystemProvisionerDescriptor {
        @Override
		public boolean discard(FilePath ws, TaskListener listener) throws IOException, InterruptedException {
            // TODO
            return false;
        }

        @Override
		public String getDisplayName() {
            return "ZFS";
        }
    }
}
