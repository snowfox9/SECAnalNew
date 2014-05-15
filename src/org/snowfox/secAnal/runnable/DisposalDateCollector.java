package org.snowfox.secAnal.runnable;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.bson.types.ObjectId;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.snowfox.secAnal.EdgarEntity;
import org.snowfox.secAnal.MongoConnector;

import java.util.ArrayList;

public class DisposalDateCollector implements Runnable {

    public void run()
    {
        ObjectId _id;
        String path = "http://www.sec.gov/Archives/edgar/data/1288776/000128877614000034/0001288776-14-000034-index.htm";
        int cik;
        int numRows;

        // do something
        synchronized (Object.class)
        {
            /*
            DBObject object = MongoConnector.queryFindOne("disposals", new BasicDBObject().append("content", 0));
            _id = new ObjectId(object.get("_id").toString());
            path = object.get("href").toString();
            numRows = MongoConnector.queryUpdate("cik", new BasicDBObject().append("_id", _id), new BasicDBObject().append("$set", new BasicDBObject().append("content", 1)), false, false, null);
            */
        }

        try
        {
            Document document = Jsoup.connect(path).get();
            Elements elements = document.select("table.tableFile tbody tr td");

            int i=0;
            String[] contents = new String[150];
            ArrayList<EdgarEntity> documents = new ArrayList<EdgarEntity>();

            for(Element e : elements)
            {
                contents[i] = e.text();
                i++;
                if(i == 5)
                {
                    documents.add(new EdgarEntity(contents[0], contents[1], contents[2], contents[3], contents[4]));
                    i = 0;
                }
            }

            path = path.substring(0, path.lastIndexOf("/")) + "/";
            String filePath;
            for(EdgarEntity e : documents)
            {
                if(e.Type.equals("8-K"))
                {
                    filePath = path + e.Document;

                    System.out.println("inspecting " + filePath);
                    document = Jsoup.connect(filePath).get();
                    Elements elements1 = document.getAllElements();

                    i=0;
                    int strLength = 0;
                    for(Element e1 : elements1)
                    {
                        String ownText = e1.ownText().trim();
                        if(ownText.length() > 0)
                        {
                            if(ownText.contains("2.02") || i >= 1)
                            {
                                // start collecting data
                                contents[i] = ownText; i++;
                                if(i>1)
                                {
                                    System.out.print(ownText + " ");
                                    strLength += ownText.length();
                                    if(strLength >= 200) break;
                                }
                            } else {
                                i = 0;
                                // skip this element
                            }
                        }
                    }
                    /*
                    MongoConnector.queryInsert("disposals", new BasicDBObject().append("$set",
                            new BasicDBObject().append("content", 2).append("firstString", ""))
                            , null);*/
                    System.out.println();
                }
            }

            // System.out.println(elements);
        } catch (Exception e)
        {
            e.printStackTrace();
        }

    }
}
