/*
 * Copyright 2012 Twitter, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.twitter.tokyo.kucho.daemon;

import com.twitter.tokyo.kucho.SeatingList;
import com.twitter.tokyo.kucho.VentilationModule;
import twitter4j.*;
import twitter4j.internal.logging.Logger;

import java.util.List;

/*package*/ final class DaemonStatusListener extends StatusAdapter implements Constants {
    private final EHills ehills;
    private final SeatingList seatingList;

    /*package*/ DaemonStatusListener(EHills ehills, SeatingList seatingList) {
        this.ehills = ehills;
        this.seatingList = seatingList;
    }

    static final Logger logger = Logger.getLogger(DaemonStatusListener.class);

    @Override
    public void onStatus(Status status) {
        String screenName = status.getUser().getScreenName();
        String message = null;
        String[] modules = getModules(screenName);
        if (status.getText().contains(HOT)) {
            ehills.cooler(modules);
            message = "@" + screenName + " 涼しくしたよ！";
        } else if (status.getText().contains(COLD)) {
            ehills.warmer(modules);
            message = "@" + screenName + " 暖かくしたよ！";
        }
        if (message != null) {
            try {
                TwitterFactory.getSingleton().updateStatus(message);
            } catch (TwitterException e) {
                logger.error("failed to update status", e);
            }
        }
    }

    private String[] getModules(String screenName) {
        List<VentilationModule> ventilationModules = seatingList.getVentilationModules(screenName);
        return new String[ventilationModules.size()];
    }

    @Override
    public void onException(Exception e) {
        logger.error("error", e);
    }
}