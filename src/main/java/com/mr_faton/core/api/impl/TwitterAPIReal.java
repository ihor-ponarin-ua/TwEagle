package com.mr_faton.core.api.impl;

import com.mr_faton.core.api.TwitterAPI;
import com.mr_faton.core.dao.UserDAO;
import com.mr_faton.core.exception.LimitExhausted;
import com.mr_faton.core.exception.NoSuchEntityException;
import org.apache.log4j.Logger;
import twitter4j.*;
import twitter4j.auth.AccessToken;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TwitterAPIReal implements TwitterAPI{
    private static final Logger logger = Logger.getLogger("" +
            "com.mr_faton.core.api.impl.TwitterAPIReal");
    private static final Map<String, Twitter> TWITTER_STORAGE = new ConcurrentHashMap<>();
    private final UserDAO userDAO;
    private static final String APP_LIMIT = "/application/rate_limit_status";


    public TwitterAPIReal(UserDAO userDAO) {
        logger.info("constructor");
        this.userDAO = userDAO;
    }

    @Override
    public long postTweet(String userName, String tweet) throws TwitterException, SQLException, NoSuchEntityException {
        logger.debug("post tweet for user " + userName);
        canWork(userName);
        Twitter twitter = getTwitter(userName);
        Status status = twitter.updateStatus(tweet);
        long tweetId = status.getId();
        logger.debug("tweet posted with id " + tweetId);
        return tweetId;
    }

    @Override
    public void deleteTweetById(String userName, long id) throws TwitterException, SQLException, NoSuchEntityException {
        logger.debug("delete tweet for user " + userName + " by id " + id);
        canWork(userName);
        Twitter twitter = getTwitter(userName);
        twitter.destroyStatus(id);
    }

    @Override
    public void deleteLastTweet(String userName) throws TwitterException, SQLException, NoSuchEntityException {
        logger.debug("delete last tweet fo user " + userName);
        canWork(userName);
        Twitter twitter = getTwitter(userName);
        ResponseList<Status> homeTimeline = twitter.getUserTimeline(new Paging(1, 1));
        Status lastStatus = homeTimeline.get(0);
        String text = lastStatus.getText();
        long id = lastStatus.getId();
        twitter.destroyStatus(id);
        logger.debug("tweet: " + text + ", id: " + id + " was deleted");
    }



    private int getAppLimit(String userName) throws TwitterException, SQLException, NoSuchEntityException {
        logger.debug("get application limit status for user " + userName);
        Twitter twitter = getTwitter(userName);
        Map<String, RateLimitStatus> rateLimitStatus = twitter.getRateLimitStatus();
        int remainLimit = rateLimitStatus.get(APP_LIMIT).getRemaining();
        logger.debug(remainLimit + " twitter queries left");
        return remainLimit;
    }

    private int getSecondsUntilResetAppLimit(String userName) throws TwitterException, SQLException, NoSuchEntityException {
        logger.debug("get seconds until reset application limit for user " + userName);
        Twitter twitter = getTwitter(userName);
        Map<String, RateLimitStatus> rateLimitStatus = twitter.getRateLimitStatus();
        int secondsUntilReset = rateLimitStatus.get(APP_LIMIT).getSecondsUntilReset();
        logger.debug(secondsUntilReset + " left to reset application limit");
        return secondsUntilReset;
    }

    private Twitter getTwitter(String userName) throws SQLException, TwitterException, NoSuchEntityException {
        logger.debug("get twitter object for user " + userName);
        if (TWITTER_STORAGE.containsKey(userName)) {
            return TWITTER_STORAGE.get(userName);
        }
        logger.debug("no twitter by name " + userName + ", must create it");

        com.mr_faton.core.table.User user = userDAO.getUserByName(userName);

        String consumerKey = user.getConsumerKey();
        String consumerSecret = user.getConsumerSecret();
        String accessToken = user.getAccessToken();
        String accessTokenSecret = user.getAccessTokenSecret();

        Twitter twitter = new TwitterFactory().getInstance();
        twitter.setOAuthConsumer(consumerKey, consumerSecret);
        twitter.setOAuthAccessToken(new AccessToken(accessToken, accessTokenSecret));

        TWITTER_STORAGE.put(userName, twitter);
        return twitter;
    }

    private void canWork(String userName) throws NoSuchEntityException, SQLException, TwitterException {
        int appLimit = getAppLimit(userName);
        if (appLimit == 0) {
            logger.warn("application limit was exhausted");
            throw new LimitExhausted("application limit was exhausted");
        }
    }
}