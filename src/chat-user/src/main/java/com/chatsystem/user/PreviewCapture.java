package com.chatsystem.user;

import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;

/** Renders actual visible Swing windows. Does not synthesize mock screenshots. */
public final class PreviewCapture {
    static Path output;
    private PreviewCapture(){}
    static void save(Window w,String name)throws Exception {
        w.validate();for(int i=0;i<3;i++)layoutTree(w);Toolkit.getDefaultToolkit().sync();
        BufferedImage image=new BufferedImage(w.getWidth(),w.getHeight(),BufferedImage.TYPE_INT_RGB);
        Graphics2D g=image.createGraphics();w.paintAll(g);g.dispose();ImageIO.write(image,"png",output.resolve(name+".png").toFile());
    }
    private static void layoutTree(Component component) {
        if(component instanceof Container container) {
            container.doLayout();
            for(Component child:container.getComponents()) layoutTree(child);
        }
    }
    public static void main(String[] args)throws Exception{
        output=Path.of(args.length==0?"screenshots":args[0]);Files.createDirectories(output);
        SwingUtilities.invokeAndWait(()->{try{
            Ui.setup();UserApp app=new UserApp();app.setVisible(true);
            for(String page:new String[]{"Tin nhắn","Bạn bè","Yêu cầu kết bạn","Tìm người"}){app.showPage(page);save(app,switch(page){case "Tin nhắn"->"01-chat";case "Bạn bè"->"02-friends";case "Yêu cầu kết bạn"->"03-requests";default->"04-people";});}
            app.showPage("Tài khoản");for(int i=0;i<5;i++){app.account.tabs.setSelectedIndex(i);save(app,"0"+(5+i)+"-account-"+i);}
            app.showPage("Tin nhắn");app.chat.selectThread(1);save(app,"10-group-chat");app.chat.selectThread(2);save(app,"11-encrypted-group");
            for(String type:new String[]{"llm","spam","create","group"}){JDialog d=app.chat.createDialog(type);d.setModalityType(Dialog.ModalityType.MODELESS);d.setVisible(true);save(d,"12-dialog-"+type);d.dispose();}
            app.setSize(1100,700);save(app,"13-minimum-size");
            // Meaningful UI smoke checks: filters, navigation and dialog construction.
            app.friends.search.setText("tranthibich");if(app.friends.table.getRowCount()!=1)throw new AssertionError("friend name filter");
            app.friends.status.setSelectedItem("Ngoại tuyến");if(app.friends.table.getRowCount()!=0)throw new AssertionError("friend status filter");
            app.chat.selectThread(0);
            app.chat.search.setText("14:00"); app.chat.searchMessages();
            if(app.chat.messages.visibleCount()!=1)throw new AssertionError("message search");
            app.chat.search.setText("không có kết quả 123"); app.chat.searchMessages();
            if(app.chat.messages.visibleCount()!=0)throw new AssertionError("empty message search");
            app.chat.search.setText("14:00"); app.chat.scope.setSelectedIndex(1); app.chat.searchMessages();
            if(app.chat.messages.visibleCount()!=3)throw new AssertionError("global message search");
            app.chat.scope.setSelectedIndex(0); app.chat.search.setText("14:00"); app.chat.showSearch(true); app.chat.searchMessages();
            app.setSize(1440,900); save(app,"14-message-search");
            app.chat.conversationList.search.setText("Trần Thị");
            if(app.chat.conversationList.list.getModel().getSize()!=1)throw new AssertionError("conversation search");
            app.chat.conversationList.search.setText("");
            app.chat.showGroups();
            if(app.chat.conversationList.list.getModel().getSize()!=3)throw new AssertionError("group filter");
            System.out.println("PASS: rendered 17 windows; friend filters, local/global/empty message search, conversation search and group filter verified");app.dispose();
        }catch(Exception e){throw new RuntimeException(e);}});
    }
}
