package com.ytj.project_login;

import android.content.Context;
import android.view.View;

import com.ytj.project_login.db.dao.DBDao;
import com.ytj.project_login.jsonEntity.ChatMsg;
import com.ytj.project_login.utils.ConstantUtil;

import java.util.List;

public class TeamChatActivity extends BaseChatActivity {

    {
        type = 1;
    }

    @Override
    public int getChatType() {
        return ConstantUtil.TEAM_CHAT_TYPE;
    }

    @Override
    public String getTitleEnd() {
        return "聊天群";
    }

    @Override
    public String getNewInfoUrl(String mIp, int fromId, int toId) {
        return "http://" + mIp + "/MapLocal/android/getChat?deptid=" + toId + "&type=" + ConstantUtil.TEAM_CHAT_TYPE;
    }

    @Override
    public String getUrl(String mIp, int fromId, int toId, int chatMsgMaxId) {
        return "http://" + mIp + "/MapLocal/android/getChat?deptid=" + toId + "&type=" + ConstantUtil.TEAM_CHAT_TYPE + "&maxid=" + chatMsgMaxId;
    }

    @Override
    public int getChatMsgMaxId(DBDao dbDao, int fromId, int toId) {
        return dbDao.getTeamChatMsgMaxId(ConstantUtil.TEAM_CHAT_TYPE, toId + "");
    }

    @Override
    public void checkLocation(Context context, String chatname, String tel, View mCheckLocation) {
    }

    @Override
    public List<ChatMsg> getRefreshChatMsgList(DBDao dbDao, String fromnum, String tonum, int limit, int offset) {
        List<ChatMsg> chatMsgs = null;
        chatMsgs = dbDao.getTeamChatMsg(ConstantUtil.TEAM_CHAT_TYPE, tonum, limit, offset);
        return chatMsgs;
    }


}
