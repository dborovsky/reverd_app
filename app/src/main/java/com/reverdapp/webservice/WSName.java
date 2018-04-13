package com.reverdapp.webservice;

/**
 * Created by wojci on 9/3/15.
 */

// WS names used for calling the different services.
public abstract class WSName {
    final static String WHITELIST_ADD = "addtowl";
    final static String BLACKLIST_ADD = "addtobl";

    // Used for moving between the two lists. Which list depends on parameters.
    final static String MOVE_FROM_LIST_TO_ANOTHER = "move";

    final static String WL_DELETE = "wldelete";
    final static String BL_DELETE = "bldelete";

    final static String REGISTER = "register";

    final static String SYNC = "sync";

    final static String FEEDBACK = "feedback";

    final static String DETAILS = "viewphonedetails";

    final static String COUNTRIES = "countries";

    final static String ADDCOMPLAINT = "addcomplaint";


    final static String BLACKLIST = "blacklist";
    final static String WHITELIST = "whitelist";

    final static String SUBSCRIPTION_CHECK = "checksubscription";

    final static String SUBSCRIBE = "subscription";
}
