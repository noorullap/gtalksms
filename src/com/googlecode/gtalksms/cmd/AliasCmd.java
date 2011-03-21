package com.googlecode.gtalksms.cmd;

import java.util.ArrayList;

import com.googlecode.gtalksms.MainService;
import com.googlecode.gtalksms.R;
import com.googlecode.gtalksms.data.phone.Phone;
import com.googlecode.gtalksms.tools.AliasHelper;
import com.googlecode.gtalksms.xmpp.XmppMsg;

/**
 * XMPP Frontend command class for the alias feature This Class handles the
 * inserting/updating/deleting of aliases via XMPP Commands
 * 
 * @author Florian Schmaus fschmaus@gmail.com - on behalf of the GTalkSMS Team
 * 
 */
public class AliasCmd extends Command {
    private AliasHelper aliasHelper;

    public AliasCmd(MainService mainService) {
        super(mainService, new String[] { "alias" });
        this.aliasHelper = mainService.createAndGetAliasHelper();
    }

    @Override
    protected void execute(String cmd, String args) {
        String[] subCommand = splitArgs(args);
        if (subCommand.length < 2) {
            send("Alias needs more arguments");
        } else {
            if (subCommand[0].equals("add")) {
                add(subCommand);
            } else if (subCommand[0].equals("del")) {
                del(subCommand);
            } else if (subCommand[0].equals("show")) {
                show(subCommand);
            }
        }
    }
    
    private void add(String[] subCommand) {
            if (Phone.isCellPhoneNumber(subCommand[2])) {
                aliasHelper.addAliasByNumber(subCommand[1], subCommand[2]);
                send(R.string.chat_alias_add_by_number, subCommand[1], subCommand[2]);
            } else {
                ArrayList<Phone> res = aliasHelper.addAliasByName(subCommand[1], subCommand[2]);
                if (res.size() != 1) {
                    send(R.string.chat_error_unkown_name);
                } else {
                    Phone p = res.get(0);
                    send(R.string.chat_alias_add_by_name, subCommand[1], p.contactName, p.number);
                }
            }
    }
    
    private void del(String[] subCommand) {
        if (aliasHelper.deleteAlias(subCommand[1])) {
            send(R.string.chat_alias_del_suc, subCommand[1]);
        } else {
            send(R.string.chat_alias_del_suc, subCommand[1]);
        }
    }
    
    private void show(String[] subCommand) {
        if (subCommand[1].equals("all")) {
            String[][] aliases = aliasHelper.getAllAliases();
            if (aliases == null) {
                send(R.string.chat_alias_empty);
            } else {
                XmppMsg msg = new XmppMsg();
                for (int i = 0; i < aliases.length; i++) {
                    msg.appendBold("Alias: " + aliases[i][0] + " ");
                    if (aliases[i][2] == null) {
                        msg.appendLine(aliases[i][1]);
                    } else {
                        msg.append(aliases[i][1] + " - ");
                        msg.appendLine(aliases[i][2]);
                    }
                }
                send(msg);
            }
        } else {
            String[] res = aliasHelper.getAliasOrNull(subCommand[1]);
            if (res == null) {
                send(R.string.chat_alias_show_non_existent, subCommand[1]);
            } else if (res.length == 2) {
                send("\'" + res[0] + "\' -> " + res[1]);
            } else {
                send("\'" + res[0] + "\' - " + res[2] + " -> " + res[1]);
            }
        }
    }

    @Override
    public String[] help() {
        String[] s = {
                getString(R.string.chat_help_alias_general, makeBold("\"alias:subCommand:argument")),
                getString(R.string.chat_help_alias_add, makeBold("\"alias:add:#aliasname#:#contact#")),
                getString(R.string.chat_help_alias_show, makeBold("\"alias:show:#aliasname#\"")),
                getString(R.string.chat_help_alias_del, makeBold("\"alias:del:#aliasname#\""))
        };
        return s;
    }

}
