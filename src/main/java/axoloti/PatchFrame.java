/**
 * Copyright (C) 2013 - 2016 Johannes Taelman
 *
 * This file is part of Axoloti.
 *
 * Axoloti is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * Axoloti is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Axoloti. If not, see <http://www.gnu.org/licenses/>.
 */
package axoloti;

import axoloti.dialogs.AboutFrame;
import axoloti.object.AxoObjects;
import axoloti.utils.Constants;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.DefaultEditorKit;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import qcmds.QCmdLock;
import qcmds.QCmdProcessor;
import qcmds.QCmdStart;
import qcmds.QCmdStop;
import qcmds.QCmdUploadPatch;

/**
 *
 * @author Johannes Taelman
 */
public class PatchFrame extends javax.swing.JFrame implements DocumentWindow, ConnectionStatusListener {

    /**
     * Creates new form PatchFrame
     */
    PatchGUI patch;

    public PatchFrame(final PatchGUI patch, QCmdProcessor qcmdprocessor) {
        setIconImage(new ImageIcon(getClass().getResource("/resources/axoloti_icon.png")).getImage());
        this.qcmdprocessor = qcmdprocessor;
        initComponents();
        this.patch = patch;
        this.patch.patchframe = this;
        DocumentWindowList.RegisterWindow(this);
        USBBulkConnection.GetConnection().addConnectionStatusListener(this);

        jToolbarPanel.add(new components.PresetPanel(patch));
        jToolbarPanel.add(new javax.swing.Box.Filler(new Dimension(0, 0), new Dimension(0, 0), new Dimension(32767, 32767)));
        jScrollPane1.setViewportView(patch.Layers);
        jScrollPane1.getVerticalScrollBar().setUnitIncrement(Constants.ygrid / 2);
        jScrollPane1.getHorizontalScrollBar().setUnitIncrement(Constants.xgrid / 2);

        JMenuItem menuItem = new JMenuItem(new DefaultEditorKit.CutAction());
        menuItem.setText("Cut");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        jMenuEdit.add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Patch p = patch.GetSelectedObjects();
                if (p.objectinstances.isEmpty()) {
                    getToolkit().getSystemClipboard().setContents(new StringSelection(""), null);
                    return;
                }
                p.PreSerialize();
                Serializer serializer = new Persister();
                try {
                    Clipboard clip = getToolkit().getSystemClipboard();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    serializer.write(p, baos);
                    StringSelection s = new StringSelection(baos.toString());
                    clip.setContents(s, (ClipboardOwner) null);
                    patch.deleteSelectedAxoObjInstances();
                } catch (Exception ex) {
                    Logger.getLogger(AxoObjects.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        menuItem = new JMenuItem(new DefaultEditorKit.CopyAction());
        menuItem.setText("Copy");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        jMenuEdit.add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Patch p = patch.GetSelectedObjects();
                if (p.objectinstances.isEmpty()) {
                    getToolkit().getSystemClipboard().setContents(new StringSelection(""), null);
                    return;
                }
                p.PreSerialize();
                Serializer serializer = new Persister();
                try {
                    Clipboard clip = getToolkit().getSystemClipboard();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    serializer.write(p, baos);
                    StringSelection s = new StringSelection(baos.toString());
                    clip.setContents(s, (ClipboardOwner) null);
                } catch (Exception ex) {
                    Logger.getLogger(AxoObjects.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        menuItem = new JMenuItem(new DefaultEditorKit.PasteAction());
        menuItem.setText("Paste");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        jMenuEdit.add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Clipboard clip = getToolkit().getSystemClipboard();
                try {
                    patch.paste((String) clip.getData(DataFlavor.stringFlavor), null, false);
                } catch (UnsupportedFlavorException ex) {
                    Logger.getLogger(PatchFrame.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(PatchFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        if (patch.getWindowPos() != null) {
            setBounds(patch.getWindowPos());
        } else {
            Dimension d = patch.GetInitialSize();
            setSize(d);
        }

        if (!MainFrame.prefs.getExpertMode()) {
            jSeparator3.setVisible(false);
            jMenuItemLock.setVisible(false);
            jMenuGenerateCode.setVisible(false);
            jMenuCompileCode.setVisible(false);
            jMenuUploadCode.setVisible(false);
            jMenuItemLock.setVisible(false);
            jMenuItemUnlock.setVisible(false);
        }
        jMenuPreset.setVisible(false);
        jMenuItemAdjScroll.setVisible(false);
        patch.Layers.requestFocus();
        if (USBBulkConnection.GetConnection().isConnected()) {
            ShowConnect();
        }

        //        jScrollPane1.setAutoscrolls(true);
        /*
         patch.setPreferredSize(new Dimension(5000, 5000));
         jScrollPane1.getViewport().setSize(5000, 5000);
         patch.setSize(new Dimension(5000, 5000));
         patch.setMinimumSize(new Dimension(5000, 5000));
         patch.setBackground(Color.red);
         patch.invalidate();*/
    }
    QCmdProcessor qcmdprocessor;

    public void SetLive(boolean b) {
        if (b) {
            jCheckBoxLive.setSelected(true);
            jCheckBoxLive.setEnabled(true);
            jCheckBoxMenuItemLive.setSelected(true);
            jCheckBoxMenuItemLive.setEnabled(true);
        } else {
            jCheckBoxLive.setSelected(false);
            jCheckBoxLive.setEnabled(true);
            jCheckBoxMenuItemLive.setSelected(false);
            jCheckBoxMenuItemLive.setEnabled(true);
        }
    }

    @Override
    public void ShowDisconnect() {
        if (patch.IsLocked()) {
            patch.Unlock();
        }
        jCheckBoxLive.setEnabled(false);
        jCheckBoxLive.setSelected(false);
        jCheckBoxMenuItemLive.setEnabled(false);
        jCheckBoxMenuItemLive.setSelected(false);
    }

    @Override
    public void ShowConnect() {
        patch.Unlock();
        jCheckBoxLive.setSelected(false);
        jCheckBoxLive.setEnabled(true);
        jCheckBoxMenuItemLive.setSelected(false);
        jCheckBoxMenuItemLive.setEnabled(true);
    }

    public void ShowCompileFail() {
        jCheckBoxLive.setSelected(false);
        jCheckBoxLive.setEnabled(true);
    }

    public void Close() {
        DocumentWindowList.UnregisterWindow(this);
        USBBulkConnection.GetConnection().removeConnectionStatusListener(this);
        dispose();
    }

    @Override
    public boolean AskClose() {
        if (patch.isDirty() && patch.container() == null) {
            Object[] options = {"Save",
                "Don't save",
                "Cancel"};
            int n = JOptionPane.showOptionDialog(this,
                    "Do you want to save changes to " + patch.getFileNamePath() + " ?",
                    "Axoloti asks:",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[2]);
            switch (n) {
                case JOptionPane.YES_OPTION:
                    jMenuSaveActionPerformed(null);
                    Close();
                    return false;
                case JOptionPane.NO_OPTION:
                    Close();
                    return false;
                case JOptionPane.CANCEL_OPTION:
                    return true;
                default:
                    return false;
            }
        } else {
            Close();
            return false;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToolbarPanel = new javax.swing.JPanel();
        jCheckBoxLive = new javax.swing.JCheckBox();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(5, 0), new java.awt.Dimension(5, 0), new java.awt.Dimension(5, 0));
        jLabel1 = new javax.swing.JLabel();
        jProgressBarDSPLoad = new javax.swing.JProgressBar();
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(5, 0), new java.awt.Dimension(5, 0), new java.awt.Dimension(5, 0));
        jScrollPane1 = new javax.swing.JScrollPane();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenuFile = new javax.swing.JMenu();
        jMenuNew = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        jMenuOpen = new javax.swing.JMenuItem();
        jMenuOpenURL = new javax.swing.JMenuItem();
        recentFileMenu1 = new axoloti.menus.RecentFileMenu();
        libraryMenu1 = new axoloti.menus.LibraryMenu();
        favouriteMenu1 = new axoloti.menus.FavouriteMenu();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        jMenuSave = new javax.swing.JMenuItem();
        jMenuSaveAs = new javax.swing.JMenuItem();
        jMenuSaveClip = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        jMenuClose = new javax.swing.JMenuItem();
        jMenuQuit = new javax.swing.JMenuItem();
        jMenuEdit = new javax.swing.JMenu();
        jMenuItemDelete = new javax.swing.JMenuItem();
        jMenuItemSelectAll = new javax.swing.JMenuItem();
        jMenuItemAddObj = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        jMenuView = new javax.swing.JMenu();
        jMenuItemNotes = new javax.swing.JMenuItem();
        jMenuItemSettings = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        jCheckBoxMenuItemCordsInBackground = new javax.swing.JCheckBoxMenuItem();
        jMenuItemAdjScroll = new javax.swing.JMenuItem();
        jMenuPatch = new javax.swing.JMenu();
        jCheckBoxMenuItemLive = new javax.swing.JCheckBoxMenuItem();
        jMenuItemUploadSD = new javax.swing.JMenuItem();
        jMenuItemUploadSDStart = new javax.swing.JMenuItem();
        jMenuItemUploadInternalFlash = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        jMenuGenerateCode = new javax.swing.JMenuItem();
        jMenuCompileCode = new javax.swing.JMenuItem();
        jMenuUploadCode = new javax.swing.JMenuItem();
        jMenuItemLock = new javax.swing.JMenuItem();
        jMenuItemUnlock = new javax.swing.JMenuItem();
        jMenuPreset = new javax.swing.JMenu();
        jMenuItemClearPreset = new javax.swing.JMenuItem();
        jMenuItemPresetCurrentToInit = new javax.swing.JMenuItem();
        jMenuItemDifferenceToPreset = new javax.swing.JMenuItem();
        windowMenu1 = new axoloti.menus.WindowMenu();
        helpMenu1 = new axoloti.menus.HelpMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                formFocusLost(evt);
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.Y_AXIS));

        jToolbarPanel.setAlignmentX(1.0F);
        jToolbarPanel.setAlignmentY(0.0F);
        jToolbarPanel.setMaximumSize(new java.awt.Dimension(32767, 0));
        jToolbarPanel.setPreferredSize(new java.awt.Dimension(212, 49));
        jToolbarPanel.setLayout(new javax.swing.BoxLayout(jToolbarPanel, javax.swing.BoxLayout.LINE_AXIS));

        jCheckBoxLive.setText("Live");
        jCheckBoxLive.setEnabled(false);
        jCheckBoxLive.setFocusable(false);
        jCheckBoxLive.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jCheckBoxLive.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jCheckBoxLive.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jCheckBoxLive.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxLiveActionPerformed(evt);
            }
        });
        jToolbarPanel.add(jCheckBoxLive);

        filler2.setAlignmentX(0.0F);
        jToolbarPanel.add(filler2);

        jLabel1.setText("DSP load ");
        jToolbarPanel.add(jLabel1);

        jProgressBarDSPLoad.setToolTipText("");
        jProgressBarDSPLoad.setAlignmentX(0.0F);
        jProgressBarDSPLoad.setMaximumSize(new java.awt.Dimension(100, 16));
        jProgressBarDSPLoad.setMinimumSize(new java.awt.Dimension(60, 16));
        jProgressBarDSPLoad.setName(""); // NOI18N
        jProgressBarDSPLoad.setPreferredSize(new java.awt.Dimension(100, 16));
        jProgressBarDSPLoad.setStringPainted(true);
        jToolbarPanel.add(jProgressBarDSPLoad);

        filler3.setAlignmentX(0.0F);
        jToolbarPanel.add(filler3);

        getContentPane().add(jToolbarPanel);

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        jScrollPane1.setAutoscrolls(true);
        getContentPane().add(jScrollPane1);

        jMenuFile.setMnemonic('F');
        jMenuFile.setText("File");

        jMenuNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    jMenuNew.setText("New");
    jMenuNew.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jMenuNewActionPerformed(evt);
        }
    });
    jMenuFile.add(jMenuNew);
    jMenuFile.add(jSeparator6);

    jMenuOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
jMenuOpen.setText("Open...");
jMenuOpen.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        jMenuOpenActionPerformed(evt);
    }
    });
    jMenuFile.add(jMenuOpen);

    jMenuOpenURL.setText("Open from URL...");
    jMenuOpenURL.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jMenuOpenURLActionPerformed(evt);
        }
    });
    jMenuFile.add(jMenuOpenURL);

    recentFileMenu1.setText("Open Recent");
    jMenuFile.add(recentFileMenu1);

    libraryMenu1.setText("Library");
    jMenuFile.add(libraryMenu1);

    favouriteMenu1.setText("Favorites");
    jMenuFile.add(favouriteMenu1);
    jMenuFile.add(jSeparator5);

    jMenuSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
jMenuSave.setText("Save");
jMenuSave.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        jMenuSaveActionPerformed(evt);
    }
    });
    jMenuFile.add(jMenuSave);

    jMenuSaveAs.setText("Save As...");
    jMenuSaveAs.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jMenuSaveAsActionPerformed(evt);
        }
    });
    jMenuFile.add(jMenuSaveAs);

    jMenuSaveClip.setText("Save To Clipboard");
    jMenuSaveClip.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jMenuSaveClipActionPerformed(evt);
        }
    });
    jMenuFile.add(jMenuSaveClip);
    jMenuFile.add(jSeparator1);

    jMenuClose.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,
        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
jMenuClose.setText("Close");
jMenuClose.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        jMenuCloseActionPerformed(evt);
    }
    });
    jMenuFile.add(jMenuClose);

    jMenuQuit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
jMenuQuit.setText("Quit");
jMenuQuit.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        jMenuQuitActionPerformed(evt);
    }
    });
    jMenuFile.add(jMenuQuit);

    jMenuBar1.add(jMenuFile);

    jMenuEdit.setMnemonic('E');
    jMenuEdit.setText("Edit");

    jMenuItemDelete.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE, 0));
    jMenuItemDelete.setText("Delete");
    jMenuItemDelete.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jMenuItemDeleteActionPerformed(evt);
        }
    });
    jMenuEdit.add(jMenuItemDelete);

    jMenuItemSelectAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
jMenuItemSelectAll.setText("Select All");
jMenuItemSelectAll.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        jMenuItemSelectAllActionPerformed(evt);
    }
    });
    jMenuEdit.add(jMenuItemSelectAll);

    jMenuItemAddObj.setText("New Object...");
    jMenuItemAddObj.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jMenuItemAddObjActionPerformed(evt);
        }
    });
    jMenuEdit.add(jMenuItemAddObj);
    jMenuEdit.add(jSeparator4);

    jMenuBar1.add(jMenuEdit);

    jMenuView.setMnemonic('V');
    jMenuView.setText("View");

    jMenuItemNotes.setText("Notes");
    jMenuItemNotes.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jMenuItemNotesActionPerformed(evt);
        }
    });
    jMenuView.add(jMenuItemNotes);

    jMenuItemSettings.setText("Settings");
    jMenuItemSettings.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jMenuItemSettingsActionPerformed(evt);
        }
    });
    jMenuView.add(jMenuItemSettings);
    jMenuView.add(jSeparator2);

    jCheckBoxMenuItemCordsInBackground.setText("Patch Cords In Background");
    jCheckBoxMenuItemCordsInBackground.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jCheckBoxMenuItemCordsInBackgroundActionPerformed(evt);
        }
    });
    jMenuView.add(jCheckBoxMenuItemCordsInBackground);

    jMenuItemAdjScroll.setText("Adjust Scroll");
    jMenuItemAdjScroll.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jMenuItemAdjScrollActionPerformed(evt);
        }
    });
    jMenuView.add(jMenuItemAdjScroll);

    jMenuBar1.add(jMenuView);

    jMenuPatch.setMnemonic('P');
    jMenuPatch.setText("Patch");

    jCheckBoxMenuItemLive.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,
        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
jCheckBoxMenuItemLive.setText("Live");
jCheckBoxMenuItemLive.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        jCheckBoxMenuItemLiveActionPerformed(evt);
    }
    });
    jMenuPatch.add(jCheckBoxMenuItemLive);

    jMenuItemUploadSD.setText("Upload to SDCard");
    jMenuItemUploadSD.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jMenuItemUploadSDActionPerformed(evt);
        }
    });
    jMenuPatch.add(jMenuItemUploadSD);

    jMenuItemUploadSDStart.setText("Upload to SDCard as startup");
    jMenuItemUploadSDStart.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jMenuItemUploadSDStartActionPerformed(evt);
        }
    });
    jMenuPatch.add(jMenuItemUploadSDStart);

    jMenuItemUploadInternalFlash.setText("Upload to internal flash");
    jMenuItemUploadInternalFlash.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jMenuItemUploadInternalFlashActionPerformed(evt);
        }
    });
    jMenuPatch.add(jMenuItemUploadInternalFlash);
    jMenuPatch.add(jSeparator3);

    jMenuGenerateCode.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G,
        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
jMenuGenerateCode.setText("Generate code");
jMenuGenerateCode.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        jMenuGenerateCodeActionPerformed(evt);
    }
    });
    jMenuPatch.add(jMenuGenerateCode);

    jMenuCompileCode.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H,
        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
jMenuCompileCode.setText("Compile code");
jMenuCompileCode.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        jMenuCompileCodeActionPerformed(evt);
    }
    });
    jMenuPatch.add(jMenuCompileCode);

    jMenuUploadCode.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_J,
        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
jMenuUploadCode.setText("Upload code");
jMenuUploadCode.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        jMenuUploadCodeActionPerformed(evt);
    }
    });
    jMenuPatch.add(jMenuUploadCode);

    jMenuItemLock.setText("Lock");
    jMenuItemLock.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jMenuItemLockActionPerformed(evt);
        }
    });
    jMenuPatch.add(jMenuItemLock);

    jMenuItemUnlock.setText("Unlock");
    jMenuItemUnlock.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jMenuItemUnlockActionPerformed(evt);
        }
    });
    jMenuPatch.add(jMenuItemUnlock);

    jMenuBar1.add(jMenuPatch);

    jMenuPreset.setText("Preset");
    jMenuPreset.setEnabled(false);

    jMenuItemClearPreset.setText("Clear current preset");
    jMenuItemClearPreset.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jMenuItemClearPresetActionPerformed(evt);
        }
    });
    jMenuPreset.add(jMenuItemClearPreset);

    jMenuItemPresetCurrentToInit.setText("Copy current state to init");
    jMenuItemPresetCurrentToInit.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jMenuItemPresetCurrentToInitActionPerformed(evt);
        }
    });
    jMenuPreset.add(jMenuItemPresetCurrentToInit);

    jMenuItemDifferenceToPreset.setText("Difference between current and init to preset");
    jMenuItemDifferenceToPreset.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jMenuItemDifferenceToPresetActionPerformed(evt);
        }
    });
    jMenuPreset.add(jMenuItemDifferenceToPreset);

    jMenuBar1.add(jMenuPreset);
    jMenuBar1.add(windowMenu1);

    helpMenu1.setText("Help");
    jMenuBar1.add(helpMenu1);

    setJMenuBar(jMenuBar1);

    pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jCheckBoxLiveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxLiveActionPerformed
        if (jCheckBoxLive.isSelected()) {
            if (GoLive()) {
                jCheckBoxLive.setEnabled(false);
            } else {
                jCheckBoxLive.setSelected(false);
            }
        } else {
            qcmdprocessor.AppendToQueue(new QCmdStop());
            patch.Unlock();
        }
    }//GEN-LAST:event_jCheckBoxLiveActionPerformed

    private void jMenuOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuOpenActionPerformed
        MainFrame.mainframe.OpenPatch();
    }//GEN-LAST:event_jMenuOpenActionPerformed

    private void jMenuSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuSaveActionPerformed
        String fn = patch.getFileNamePath();
        if ((fn != null) && (!fn.equals("untitled"))) {
            File f = new File(fn);
            patch.setFileNamePath(f.getPath());
            patch.save(f);
        } else {
            jMenuSaveAsActionPerformed(evt);
        }
    }//GEN-LAST:event_jMenuSaveActionPerformed

    private void jMenuSaveAsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuSaveAsActionPerformed
        final JFileChooser fc = new JFileChooser(MainFrame.prefs.getCurrentFileDirectory());
        fc.setAcceptAllFileFilterUsed(false);
        FileFilter axp = new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.getName().endsWith("axp")) {
                    return true;
                } else if (file.isDirectory()) {
                    return true;
                }
                return false;
            }

            @Override
            public String getDescription() {
                return "Axoloti Patch";
            }
        };
        FileFilter axh = new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.getName().endsWith("axh")) {
                    return true;
                } else if (file.isDirectory()) {
                    return true;
                }
                return false;
            }

            @Override
            public String getDescription() {
                return "Axoloti Help";
            }
        };
        FileFilter axs = new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.getName().endsWith("axs")) {
                    return true;
                } else if (file.isDirectory()) {
                    return true;
                }
                return false;
            }

            @Override
            public String getDescription() {
                return "Axoloti Subpatch";
            }
        };
        fc.addChoosableFileFilter(axp);
        fc.addChoosableFileFilter(axs);
        fc.addChoosableFileFilter(axh);
        String fn = patch.getFileNamePath();
        if (fn == null) {
            fn = "untitled";
        }
        File f = new File(fn);
        fc.setSelectedFile(f);

        String ext = "";
        int dot = fn.lastIndexOf('.');
        if (dot > 0 && fn.length() > dot + 3) {
            ext = fn.substring(dot);
        }
        if (ext.equalsIgnoreCase(".axp")) {
            fc.setFileFilter(axp);
        } else if (ext.equalsIgnoreCase(".axs")) {
            fc.setFileFilter(axs);
        } else if (ext.equalsIgnoreCase(".axh")) {
            fc.setFileFilter(axh);
        } else {
            fc.setFileFilter(axp);
        }

        int returnVal = fc.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            String filterext = ".axp";
            if (fc.getFileFilter() == axp) {
                filterext = ".axp";
            } else if (fc.getFileFilter() == axs) {
                filterext = ".axs";
            } else if (fc.getFileFilter() == axh) {
                filterext = ".axh";
            }

            File fileToBeSaved = fc.getSelectedFile();
            ext = "";
            String fname = fileToBeSaved.getAbsolutePath();
            dot = fname.lastIndexOf('.');
            if (dot > 0 && fname.length() > dot + 3) {
                ext = fname.substring(dot);
            }

            if (!(ext.equalsIgnoreCase(".axp")
                    || ext.equalsIgnoreCase(".axh")
                    || ext.equalsIgnoreCase(".axs"))) {

                fileToBeSaved = new File(fc.getSelectedFile() + filterext);

            } else if (!ext.equals(filterext)) {
                Object[] options = {"Yes",
                    "No"};
                int n = JOptionPane.showOptionDialog(this,
                        "File does not match filter, do you want to change extension to " + filterext + " ?",
                        "Axoloti asks:",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[1]);
                switch (n) {
                    case JOptionPane.YES_OPTION:
                        fileToBeSaved = new File(fname.substring(0, fname.length() - 4) + filterext);
                        break;
                    case JOptionPane.NO_OPTION:
                        return;
                }
            }

            if (fileToBeSaved.exists()) {
                Object[] options = {"Yes",
                    "No"};
                int n = JOptionPane.showOptionDialog(this,
                        "File exists, do you want to overwrite ?",
                        "Axoloti asks:",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[1]);
                switch (n) {
                    case JOptionPane.YES_OPTION:
                        break;
                    case JOptionPane.NO_OPTION:
                        return;
                }
            }

            patch.setFileNamePath(fileToBeSaved.getPath());
            MainFrame.prefs.setCurrentFileDirectory(fileToBeSaved.getPath());
            patch.save(fileToBeSaved);
        }
    }//GEN-LAST:event_jMenuSaveAsActionPerformed

    private void jMenuQuitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuQuitActionPerformed
        patch.GetMainFrame().Quit();
    }//GEN-LAST:event_jMenuQuitActionPerformed

    private void jMenuItemAdjScrollActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAdjScrollActionPerformed
        jScrollPane1.setAutoscrolls(true);
        patch.AdjustSize();
    }//GEN-LAST:event_jMenuItemAdjScrollActionPerformed

    private void jCheckBoxMenuItemCordsInBackgroundActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxMenuItemCordsInBackgroundActionPerformed
        patch.SetCordsInBackground(jCheckBoxMenuItemCordsInBackground.isSelected());
    }//GEN-LAST:event_jCheckBoxMenuItemCordsInBackgroundActionPerformed

    private void jMenuGenerateCodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuGenerateCodeActionPerformed
        patch.WriteCode();
    }//GEN-LAST:event_jMenuGenerateCodeActionPerformed

    private void jMenuCompileCodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuCompileCodeActionPerformed
        patch.Compile();
    }//GEN-LAST:event_jMenuCompileCodeActionPerformed

    private void jMenuUploadCodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuUploadCodeActionPerformed
        patch.GetQCmdProcessor().SetPatch(null);
        patch.GetQCmdProcessor().AppendToQueue(new QCmdStop());
        patch.GetQCmdProcessor().AppendToQueue(new QCmdUploadPatch());
        patch.GetQCmdProcessor().AppendToQueue(new QCmdStart(patch));
        patch.GetQCmdProcessor().AppendToQueue(new QCmdLock(patch));
    }//GEN-LAST:event_jMenuUploadCodeActionPerformed

    private void jMenuItemLockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemLockActionPerformed
        patch.Lock();
    }//GEN-LAST:event_jMenuItemLockActionPerformed

    private void jMenuItemUnlockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemUnlockActionPerformed
        patch.Unlock();
    }//GEN-LAST:event_jMenuItemUnlockActionPerformed

    private void jMenuItemClearPresetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemClearPresetActionPerformed
        patch.ClearCurrentPreset();
    }//GEN-LAST:event_jMenuItemClearPresetActionPerformed

    private void jMenuItemPresetCurrentToInitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPresetCurrentToInitActionPerformed
        patch.CopyCurrentToInit();
    }//GEN-LAST:event_jMenuItemPresetCurrentToInitActionPerformed

    private void jMenuItemDifferenceToPresetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemDifferenceToPresetActionPerformed
        patch.DifferenceToPreset();
    }//GEN-LAST:event_jMenuItemDifferenceToPresetActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        AskClose();
    }//GEN-LAST:event_formWindowClosing

    private void jMenuItemDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemDeleteActionPerformed
        patch.deleteSelectedAxoObjInstances();
    }//GEN-LAST:event_jMenuItemDeleteActionPerformed

    private void jMenuNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuNewActionPerformed
        MainFrame.mainframe.NewPatch();
    }//GEN-LAST:event_jMenuNewActionPerformed

    private void jMenuItemSelectAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSelectAllActionPerformed
        patch.SelectAll();
    }//GEN-LAST:event_jMenuItemSelectAllActionPerformed

    private void jMenuItemNotesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemNotesActionPerformed
        patch.ShowNotesFrame();
    }//GEN-LAST:event_jMenuItemNotesActionPerformed

    private void jMenuItemSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSettingsActionPerformed
        if (patch.settings == null) {
            patch.settings = new PatchSettings();
        }
        patch.settings.showEditor();
    }//GEN-LAST:event_jMenuItemSettingsActionPerformed

    private void jCheckBoxMenuItemLiveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxMenuItemLiveActionPerformed
        if (jCheckBoxMenuItemLive.isSelected()) {
            if (GoLive()) {
                jCheckBoxMenuItemLive.setEnabled(false);
            } else {
                jCheckBoxMenuItemLive.setSelected(false);

            }
        } else {
            qcmdprocessor.AppendToQueue(new QCmdStop());
            patch.Unlock();
        }
    }//GEN-LAST:event_jCheckBoxMenuItemLiveActionPerformed

    private void jMenuItemUploadSDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemUploadSDActionPerformed
        patch.WriteCode();
        String FileNameNoPath = patch.getFileNamePath();
        String separator = System.getProperty("file.separator");
        int lastSeparatorIndex = FileNameNoPath.lastIndexOf(separator);
        if (lastSeparatorIndex > 0) {
            FileNameNoPath = FileNameNoPath.substring(lastSeparatorIndex + 1);
        }
        Logger.getLogger(PatchFrame.class.getName()).log(Level.INFO, "target filename:{0}", FileNameNoPath);
        qcmdprocessor.AppendToQueue(new qcmds.QCmdStop());
        qcmdprocessor.AppendToQueue(new qcmds.QCmdCompilePatch(patch));
        qcmdprocessor.AppendToQueue(new qcmds.QCmdWriteFile("0:" + FileNameNoPath));
    }//GEN-LAST:event_jMenuItemUploadSDActionPerformed

    private void jMenuItemUploadSDStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemUploadSDStartActionPerformed
        patch.WriteCode();
        String FileNameNoPath = "start.bin";
        Logger.getLogger(PatchFrame.class.getName()).log(Level.INFO, "target filename:{0}", FileNameNoPath);
        qcmdprocessor.AppendToQueue(new qcmds.QCmdStop());
        qcmdprocessor.AppendToQueue(new qcmds.QCmdCompilePatch(patch));
        qcmdprocessor.AppendToQueue(new qcmds.QCmdWriteFile("0:" + FileNameNoPath));
    }//GEN-LAST:event_jMenuItemUploadSDStartActionPerformed

    private void jMenuSaveClipActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuSaveClipActionPerformed
        Serializer serializer = new Persister();
        ByteArrayOutputStream baos = new ByteArrayOutputStream(2048);
        try {
            serializer.write(patch, baos);
        } catch (Exception ex) {
            Logger.getLogger(AxoObjects.class.getName()).log(Level.SEVERE, null, ex);
        }
        Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
        c.setContents(new StringSelection(baos.toString()), null);
    }//GEN-LAST:event_jMenuSaveClipActionPerformed

    private void jMenuItemUploadInternalFlashActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemUploadInternalFlashActionPerformed
        patch.WriteCode();
        qcmdprocessor.AppendToQueue(new qcmds.QCmdStop());
        qcmdprocessor.AppendToQueue(new qcmds.QCmdCompilePatch(patch));
        qcmdprocessor.AppendToQueue(new qcmds.QCmdUploadPatch());
        qcmdprocessor.AppendToQueue(new qcmds.QCmdCopyPatchToFlash());
    }//GEN-LAST:event_jMenuItemUploadInternalFlashActionPerformed

    private void jMenuItemAddObjActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAddObjActionPerformed
        patch.ShowClassSelector(new Point(20, 20), null, null);
    }//GEN-LAST:event_jMenuItemAddObjActionPerformed

    private void jMenuCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuCloseActionPerformed
        AskClose();
    }//GEN-LAST:event_jMenuCloseActionPerformed

    private void formFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_formFocusLost
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_formFocusLost

    private void jMenuOpenURLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuOpenURLActionPerformed
        MainFrame.mainframe.OpenURL();
    }//GEN-LAST:event_jMenuOpenURLActionPerformed

    private boolean GoLive() {

        if (patch.getFileNamePath().endsWith(".axs") || patch.container() != null) {
            Object[] options = {"Yes",
                "No"};

            int n = JOptionPane.showOptionDialog(this,
                    "This is a subpatch intended to be used by a main patch and possibly has no output. \nDo you still want to take it live?",
                    "Axoloti asks:",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[1]);
            switch (n) {
                case JOptionPane.NO_OPTION:
                    return false;
                case JOptionPane.YES_OPTION:
                    ; // fall thru
            }
        }
        patch.GoLive();
        return true;
    }

    /* write to sdcard...
     */
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private axoloti.menus.FavouriteMenu favouriteMenu1;
    private javax.swing.Box.Filler filler2;
    private javax.swing.Box.Filler filler3;
    private axoloti.menus.HelpMenu helpMenu1;
    private javax.swing.JCheckBox jCheckBoxLive;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItemCordsInBackground;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItemLive;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuClose;
    private javax.swing.JMenuItem jMenuCompileCode;
    private javax.swing.JMenu jMenuEdit;
    private javax.swing.JMenu jMenuFile;
    private javax.swing.JMenuItem jMenuGenerateCode;
    private javax.swing.JMenuItem jMenuItemAddObj;
    private javax.swing.JMenuItem jMenuItemAdjScroll;
    private javax.swing.JMenuItem jMenuItemClearPreset;
    private javax.swing.JMenuItem jMenuItemDelete;
    private javax.swing.JMenuItem jMenuItemDifferenceToPreset;
    private javax.swing.JMenuItem jMenuItemLock;
    private javax.swing.JMenuItem jMenuItemNotes;
    private javax.swing.JMenuItem jMenuItemPresetCurrentToInit;
    private javax.swing.JMenuItem jMenuItemSelectAll;
    private javax.swing.JMenuItem jMenuItemSettings;
    private javax.swing.JMenuItem jMenuItemUnlock;
    private javax.swing.JMenuItem jMenuItemUploadInternalFlash;
    private javax.swing.JMenuItem jMenuItemUploadSD;
    private javax.swing.JMenuItem jMenuItemUploadSDStart;
    private javax.swing.JMenuItem jMenuNew;
    private javax.swing.JMenuItem jMenuOpen;
    private javax.swing.JMenuItem jMenuOpenURL;
    private javax.swing.JMenu jMenuPatch;
    private javax.swing.JMenu jMenuPreset;
    private javax.swing.JMenuItem jMenuQuit;
    private javax.swing.JMenuItem jMenuSave;
    private javax.swing.JMenuItem jMenuSaveAs;
    private javax.swing.JMenuItem jMenuSaveClip;
    private javax.swing.JMenuItem jMenuUploadCode;
    private javax.swing.JMenu jMenuView;
    private javax.swing.JProgressBar jProgressBarDSPLoad;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JPopupMenu.Separator jSeparator6;
    private javax.swing.JPanel jToolbarPanel;
    private axoloti.menus.LibraryMenu libraryMenu1;
    private axoloti.menus.RecentFileMenu recentFileMenu1;
    private axoloti.menus.WindowMenu windowMenu1;
    // End of variables declaration//GEN-END:variables

    void ShowDSPLoad(int pct) {
        int pv = jProgressBarDSPLoad.getValue();
        if (pct == pv) {
            return;
        }
        if (pct == (pv - 1)) {
            return;
        }
        jProgressBarDSPLoad.setValue(pct);
    }

    @Override
    public JFrame GetFrame() {
        return this;
    }
}
