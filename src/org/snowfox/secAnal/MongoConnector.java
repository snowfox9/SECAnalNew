package org.snowfox.secAnal;

import java.util.List;

import com.mongodb.*;
import org.bson.types.ObjectId;

import java.util.List;

public class MongoConnector {

    private static boolean isReady = false;
    private static MongoClient mongoClient;
    private static DB mongoDB;

    public static boolean initiate()
    {
        try
        {
            mongoClient = new MongoClient("localhost", 27017);
            mongoDB = mongoClient.getDB("SECAnal");
        } catch(Exception e)
        {
            e.printStackTrace();
            return false;
        }

        return isReady = true;
    }

    public static boolean close()
    {
        mongoClient.close();
        isReady = false;

        return true;
    }

    public static List<DBObject> queryFind(String collectionName, DBObject condition)
    {
        DBCollection collection = mongoDB.getCollection(collectionName);
        DBCursor dbCursor = collection.find(condition);

        return dbCursor.toArray();
    }

    public static DBObject queryFindOne(String collectionName, DBObject condition)
    {
        DBCollection collection = mongoDB.getCollection(collectionName);
        return collection.findOne(condition);
    }

    private static WriteConcern writeConcernDefault = WriteConcern.FSYNC_SAFE;

    public static String queryInsert(String collectionName, DBObject document, WriteConcern writeConcern)
    {
        if(writeConcern == null) writeConcern = writeConcernDefault;
        ObjectId objectId = new ObjectId();
        DBCollection collection = mongoDB.getCollection(collectionName);
        document.put("_id", objectId);
        WriteResult writeResult = collection.insert(document, writeConcern);
        if(!writeResult.getLastError().ok())
        {
            System.out.println(writeResult.getLastError().getErrorMessage());
            return null;
        }

        return objectId.toString();
    }

    public static int queryUpdate(String collectionName, DBObject condition, DBObject document, boolean upsert, boolean multi, WriteConcern writeConcern)
    {
        if(writeConcern == null) writeConcern = writeConcernDefault;
        DBCollection collection = mongoDB.getCollection(collectionName);
        WriteResult writeResult = collection.update(condition, document, upsert, multi, writeConcern);
        if(writeResult.getLastError() != null)
        {
            throw writeResult.getLastError().getException();
        }
        return writeResult.getN();
    }

    public static int queryRemove(String collectionName, DBObject condition, WriteConcern writeConcern)
    {
        if(writeConcern == null) writeConcern = writeConcernDefault;
        DBCollection collection = mongoDB.getCollection(collectionName);
        WriteResult writeResult = collection.remove(condition, writeConcern);
        if(writeResult.getLastError() != null)
        {
            throw writeResult.getLastError().getException();
        }
        return writeResult.getN();
    }

    public static DBCollection getDBCollection(String collectionName)
    {
        // for special uses
        return mongoDB.getCollection(collectionName);
    }

}
