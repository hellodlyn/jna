/* Copyright (c) 2007 Timothy Wall, All Rights Reserved
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.  
 */
package com.sun.jna.platform;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.sun.jna.platform.win32.W32FileMonitor;

/** Provides notification of file system changes.  Actual capabilities may
 * vary slightly by platform.
 * <p>
 * Watched files which are removed from the filesystem are no longer watched.
 * @author twall
 */

public abstract class FileMonitor {

    public static final int FILE_CREATED = 0x1;
    public static final int FILE_DELETED = 0x2;
    public static final int FILE_MODIFIED = 0x4;
    public static final int FILE_ACCESSED = 0x8;
    public static final int FILE_NAME_CHANGED_OLD = 0x10;
    public static final int FILE_NAME_CHANGED_NEW = 0x20;
    public static final int FILE_RENAMED = FILE_NAME_CHANGED_OLD|FILE_NAME_CHANGED_NEW;
    public static final int FILE_SIZE_CHANGED = 0x40;
    public static final int FILE_ATTRIBUTES_CHANGED = 0x80;
    public static final int FILE_SECURITY_CHANGED = 0x100;
    public static final int FILE_ANY = 0x1FF;

    public interface FileListener {
        public void fileChanged(FileEvent e);
    }
    
    public class FileEvent extends EventObject {
        private final File file;
        private final int type;
        public FileEvent(File file, int type) {
            super(FileMonitor.this);
            this.file = file;
            this.type = type;
        }
        public File getFile() { return file; }
        public int getType() { return type; }
        public String toString() {
            return "FileEvent: " + file + ":" + type;
        }
    }
    
    private final Map watched = new HashMap();
    private List listeners = new ArrayList();
    
    protected abstract void watch(File file, int mask, boolean recursive) throws IOException ;
    protected abstract void unwatch(File file);
    public abstract void dispose();

    public void addWatch(File dir) throws IOException {
        addWatch(dir, FILE_ANY);
    }
    
    public void addWatch(File dir, int mask) throws IOException {
        addWatch(dir, mask, dir.isDirectory());
    }
    
    public void addWatch(File dir, int mask, boolean recursive) throws IOException {
        watched.put(dir, new Integer(mask));
        watch(dir, mask, recursive);
    }

    public void removeWatch(File file) {
        if (watched.remove(file) != null) {
            unwatch(file);
        }
    }
    
    protected void notify(FileEvent e) {
        for (Iterator i=listeners.iterator();i.hasNext();) {
            ((FileListener)i.next()).fileChanged(e);
        }
    }
    
    public synchronized void addFileListener(FileListener listener) {
        List list = new ArrayList(listeners);
        list.add(listener);
        listeners = list;
    }
    
    public synchronized void removeFileListener(FileListener x) {
        List list = new ArrayList(listeners);
        list.remove(x);
        listeners = list;
    }
    
    protected void finalize() {
        for (Iterator i=watched.keySet().iterator();i.hasNext();) {
            removeWatch((File)i.next());
        }
        dispose();
    }
    
    /** Canonical lazy loading of a singleton. */
    private static class Holder {
        public static final FileMonitor INSTANCE;
        static {
            String os = System.getProperty("os.name");
            if (os.startsWith("Windows")) {
                INSTANCE = new W32FileMonitor();
            }
            else {
                throw new Error("FileMonitor not implemented for " + os);
            }
        }
    }
    
    public static FileMonitor getInstance() {
        return Holder.INSTANCE;
    }
    
    private static class INotifyFileMonitor extends FileMonitor {
        protected void watch(File file, int mask, boolean recursive) {
            
        }
        
        protected void unwatch(File file) {
        
        }
        
        public void dispose() {
        	
        }
    }
}