package com.mr_faton.core.task.impl;

import com.mr_faton.core.api.TwitterAPI;
import com.mr_faton.core.dao.MessageDAO;
import com.mr_faton.core.dao.PostedMessageDAO;
import com.mr_faton.core.dao.TweetUserDAO;
import com.mr_faton.core.dao.UserDAO;
import com.mr_faton.core.exception.NoSuchEntityException;
import com.mr_faton.core.table.Message;
import com.mr_faton.core.table.PostedMessage;
import com.mr_faton.core.table.TweetUser;
import com.mr_faton.core.task.Task;
import com.mr_faton.core.util.RandomGenerator;
import com.mr_faton.core.util.TimeWizard;
import org.apache.log4j.Logger;
import twitter4j.TwitterException;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Created by Mr_Faton on 18.09.2015.
 */
public class TweetTask implements Task {
    private static final Logger logger = Logger.getLogger("" +
            "com.mr_faton.core.task.impl.TweetTask");
    private boolean status = true;
    private TweetUser tweetUser = null;

    private final TwitterAPI twitterAPI;
    private final TweetUserDAO tweetUserDAO;
    private final MessageDAO messageDAO;
    private final PostedMessageDAO postedMessageDAO;



    public TweetTask(
            TwitterAPI twitterAPI,
            TweetUserDAO tweetUserDAO,
            MessageDAO messageDAO,
            PostedMessageDAO postedMessageDAO
    ) {
        this.twitterAPI = twitterAPI;
        this.tweetUserDAO = tweetUserDAO;
        this.messageDAO = messageDAO;
        this.postedMessageDAO = postedMessageDAO;
    }

    @Override
    public boolean getStatus() {
        logger.debug("get status => " + status);
        return status;
    }

    @Override
    public void setStatus(boolean status) {
        logger.debug("change status => " + status);
        this.status = status;
    }

    @Override
    public long getTime() {
        logger.debug("get time");
        if (tweetUser == null) {
            logger.debug("tweetUser == null");
            return Long.MAX_VALUE;
        }
        return tweetUser.getNextTweet();
    }

    private void setTime() {
        logger.debug("set time");
        if (tweetUser == null) {
            logger.debug("tweetUser == null");
            return;
        }
        tweetUser.setNextTweet(evalNextTweetTime(tweetUser));
    }

    @Override
    public void update() {
        logger.debug("update task");
        try {
            tweetUser = tweetUserDAO.getUserForTweet();
        } catch (SQLException e) {
            logger.warn("exception during load tweetUser for tweet from db");
            tweetUser = null;
            return;
        } catch (NoSuchEntityException e) {
            logger.debug("no tweetUser in db for tweet");
            tweetUser = null;
            return;
        }
    }

    public void save() {
        logger.debug("save used tweetUser into db");
        try {
            tweetUserDAO.updateUser(tweetUser);
        } catch (SQLException e) {
            logger.warn("exception during saving updating tweetUser in db");
        }
    }

    @Override
    public void setDailyParams() {
        logger.debug("set daily params");
        try {
            List<TweetUser> tweetUserList = tweetUserDAO.getUserList();
            for (TweetUser tweetUser : tweetUserList) {
                int curDay = TimeWizard.getCurDay();
                if (curDay != tweetUser.getLastUpdateDay() && tweetUser.isTweet()) {
                    tweetUser.setCurTweets(0);
                    tweetUser.setMaxTweets(evalMaxTweets(tweetUser));
                    tweetUser.setNextTweet(evalNextTweetTime(tweetUser));
                    tweetUser.setLastUpdateDay(curDay);
                }
            }
            tweetUserDAO.updateUserList(tweetUserList);
        } catch (SQLException e) {
            logger.warn("exception while updating tweet tweetUser list", e);
        } catch (NoSuchEntityException e) {
            logger.debug("no tweet users in db");
        }
    }

    @Override
    public void execute() {
        logger.info("post tweet");

        try {
            int appLimit = twitterAPI.getAppLimit(tweetUser.getName());
            if (appLimit == 0) {
                logger.warn("application limit was exhausted");
                return;
            }
            Message message = messageDAO.getMessage(tweetUser.getName(), true);
            long messageId = twitterAPI.postTweet(tweetUser.getName(), message.getMessage());

            logger.info("tweet successful posted for " + tweetUser.getName() + " with id: " + messageId + " " +
                    "and text: " + message.getMessage());

            int currentTweets = tweetUser.getCurTweets();
            int maxTweets = tweetUser.getMaxTweets();

            currentTweets++;
            tweetUser.setCurTweets(currentTweets);

            if (currentTweets != maxTweets) {
                tweetUser.setNextTweet(evalNextTweetTime(tweetUser));
            } else {
                tweetUser.setNextTweet(Long.MAX_VALUE);
            }

            messageDAO.updatePostedMessage(message);

            PostedMessage postedMessage = createPostedMessage(messageId, message);
            postedMessageDAO.savePostedMessage(postedMessage);

        } catch (TwitterException e) {
            logger.warn("exception while posting tweet", e);
        } catch (NoSuchEntityException e) {
            logger.warn("exception", e);
        } catch (SQLException e) {
            logger.warn("some sql exception", e);
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }


    private int evalMaxTweets(TweetUser tweetUser) {
        logger.debug("evaluate max tweets for tweetUser " + tweetUser.getName());
//        Date creationDate = twitterUser.getCreationDate();
//        int min = 2;
//        int max = 6;
//        int rnd = RandomGenerator.getNumber(min, max);
//        return rnd;
        return 3;
    }

    private long evalNextTweetTime(TweetUser tweetUser) {
        logger.debug("evaluate next tweet time for tweetUser " + tweetUser.getName());
        int curTweets = tweetUser.getCurTweets();
        int maxTweets = tweetUser.getMaxTweets();

        if (curTweets == maxTweets) {
            return Long.MAX_VALUE;
        }

        int min = 10;
        int max = 90;
        int rnd = RandomGenerator.getNumber(min, max);
        long nextTime = rnd * 1000 + System.currentTimeMillis();
        return nextTime;
    }

    private PostedMessage createPostedMessage(long messageId, Message message) {
        PostedMessage postedMessage = new PostedMessage();
        postedMessage.setMessage(message.getMessage());
        postedMessage.setMessageId(messageId);
        postedMessage.setTweet(message.isTweet());
        postedMessage.setOwner(message.getOwner());
        postedMessage.setRecipient(null);
        Date postedDate = new Date(System.currentTimeMillis());
        postedMessage.setPostedDate(postedDate);

        return postedMessage;
    }
}
