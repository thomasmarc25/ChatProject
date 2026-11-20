package com.chatsystem.user;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Reusable personal workspace deck embedding Member 1's actual Swing panels
 * ({@link ChatPanel}, {@link PeoplePanel}, and {@link AccountPanel}) in a single {@link CardLayout}.
 * Shared by both the standalone {@link UserApp} and the integrated {@code AdminMainFrame}
 * without duplicating UI code or creating circular Maven dependencies.
 */
public final class UserWorkspaceHost extends JPanel {

    public static final String PAGE_MESSAGES = "Tin nhắn";
    public static final String PAGE_FRIENDS = "Bạn bè";
    public static final String PAGE_GROUPS = "Nhóm chat";
    public static final String PAGE_SEARCH = "Tìm kiếm";
    public static final String PAGE_REQUESTS = "Yêu cầu kết bạn";
    public static final String PAGE_SETTINGS = "Cài đặt";

    private static final String CARD_MESSAGES = "Tin nhắn";
    private static final String CARD_FRIENDS = "Bạn bè";
    private static final String CARD_REQUESTS = "Yêu cầu kết bạn";
    private static final String CARD_PEOPLE = "Tìm người";
    private static final String CARD_ACCOUNT = "Tài khoản";

    private final CardLayout cards = new CardLayout();
    private final ChatPanel chat;
    private final PeoplePanel friends;
    private final PeoplePanel requests;
    private final PeoplePanel people;
    private final AccountPanel account;
    private final Consumer<String> onNavigationChanged;

    public UserWorkspaceHost(
            UserSessionProfile session,
            boolean signedInSettingsOnly,
            Consumer<String> onNavigationChanged,
            Runnable onSwitchToAdmin
    ) {
        setLayout(cards);
        setBackground(Ui.BG);
        this.onNavigationChanged = onNavigationChanged;

        this.chat = new ChatPanel(session);
        this.friends = new PeoplePanel("Bạn bè", this::openChatForTarget, chat::openCreateGroupDialog);
        this.requests = new PeoplePanel("Yêu cầu kết bạn", this::openChatForTarget, chat::openCreateGroupDialog);
        this.people = new PeoplePanel("Tìm người", this::openChatForTarget, chat::openCreateGroupDialog);
        this.account = new AccountPanel(
                session,
                signedInSettingsOnly,
                () -> showPersonalPage(PAGE_MESSAGES),
                onSwitchToAdmin != null ? onSwitchToAdmin : () -> {}
        );

        add(chat, CARD_MESSAGES);
        add(friends, CARD_FRIENDS);
        add(requests, CARD_REQUESTS);
        add(people, CARD_PEOPLE);
        add(account, CARD_ACCOUNT);
    }

    /**
     * Switches the visible personal page while preserving the active conversation
     * and any unsent message draft in {@link ChatPanel}.
     */
    public void showPersonalPage(String pageName) {
        if (PAGE_GROUPS.equals(pageName)) {
            cards.show(this, CARD_MESSAGES);
            chat.showGroups();
        } else if (PAGE_SEARCH.equals(pageName) || CARD_PEOPLE.equals(pageName)) {
            cards.show(this, CARD_PEOPLE);
        } else if (PAGE_SETTINGS.equals(pageName) || CARD_ACCOUNT.equals(pageName)) {
            cards.show(this, CARD_ACCOUNT);
        } else if (PAGE_FRIENDS.equals(pageName)) {
            cards.show(this, CARD_FRIENDS);
        } else if (PAGE_REQUESTS.equals(pageName)) {
            cards.show(this, CARD_REQUESTS);
        } else {
            cards.show(this, CARD_MESSAGES);
            chat.conversationList.groupsOnly(false);
        }
    }

    private void openChatForTarget(String target) {
        chat.conversationList.groupsOnly(false);
        chat.selectConversationByTarget(target);
        cards.show(this, CARD_MESSAGES);
        if (onNavigationChanged != null) {
            onNavigationChanged.accept(PAGE_MESSAGES);
        }
    }

    // --- Public inspection & verification helpers (no UI duplication) ---

    public void selectConversationByTarget(String usernameOrName) {
        chat.selectConversationByTarget(usernameOrName);
    }

    public String getSelectedConversationName() {
        return chat.current.name();
    }

    public List<String> getSelectedConversationMembers() {
        return chat.current.members();
    }

    public String getDraftText() {
        return chat.composer.getText();
    }

    public void setDraftText(String text) {
        chat.composer.setText(text);
    }

    public JDialog createChatDialog(String type) {
        return chat.createDialog(type);
    }

    public JDialog createGroupDialog(String initialMemberUsername) {
        return chat.createGroupDialog(initialMemberUsername);
    }

    public String getSelectedFriendUsername() {
        return friends.selectedUsername();
    }

    public JTextField getChatSearchField() {
        return chat.search;
    }

    public JComboBox<String> getChatSearchScopeCombo() {
        return chat.scope;
    }

    public void runChatSearch(String query, boolean allHistory) {
        chat.showSearch(true);
        chat.scope.setSelectedIndex(allHistory ? 1 : 0);
        chat.search.setText(query);
        chat.searchMessages();
    }

    public int getVisibleMessageCount() {
        return chat.messages.visibleCount();
    }

    public int getSelectedMessageCount() {
        return chat.messages.selectedCount();
    }

    public void toggleMessageSelection(String messageId) {
        chat.messages.toggleSelection(messageId);
    }

    public String getSelectionStatusText() {
        return chat.selectionLabel.getText();
    }

    public String getHideConfirmationMessage() {
        return chat.buildHideConfirmationMessage();
    }

    public int hideSelectedMessagesInMemory() {
        return chat.messages.hideSelectedMessages();
    }

    public String getHighlightedMessageId() {
        return chat.messages.highlightedMessageId();
    }

    public JScrollPane getMessageListScrollPane() {
        return chat.messages;
    }

    public JTextField getFriendsSearchField() {
        return friends.search;
    }

    public JComboBox<String> getFriendsStatusCombo() {
        return friends.status;
    }

    public JTable getFriendsTable() {
        return friends.table;
    }

    public JTable getRequestsTable() {
        return requests.table;
    }

    public JTable getPeopleTable() {
        return people.table;
    }

    public JTabbedPane getAccountTabbedPane() {
        return account.tabs;
    }
}
