package org.snowfox.secAnal.runnable;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.bson.types.ObjectId;
import org.snowfox.secAnal.MongoConnector;
import org.snowfox.secAnal.RunCrawler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class DisposalCollector implements Runnable {

    @Override
    public void run() {

        int numRows;
        ObjectId _id;
        int cik;

        synchronized (String.class)
        {
            DBObject object = MongoConnector.queryFindOne("cik", new BasicDBObject().append("status", 0));
            _id = new ObjectId(object.get("_id").toString());
            cik = (int) Float.parseFloat(object.get("cik").toString());
            numRows = MongoConnector.queryUpdate("cik", new BasicDBObject().append("_id", _id), new BasicDBObject().append("status", 1), false, false, null);
        }

        try
        {
            if(numRows > 0)
            {
                // do search and insert

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document;
                int currentCount = 0;

                while(true)
                {
                    String rssFeedPath = String.format(RunCrawler.getInfoSite, cik, currentCount, 100);

                    // if # of entries = 0 -> terminate searching
                    document = builder.parse(rssFeedPath);

                    NodeList nodeList = document.getDocumentElement().getElementsByTagName("company-info");
                    Element element = (Element) nodeList.item(0);
                    NodeList nodeCompanyName = element.getElementsByTagName("conformed-name");
                    String companyName = nodeCompanyName.item(0).getTextContent();

                    nodeList = document.getDocumentElement().getElementsByTagName("entry");

                    if(nodeList.getLength() == 0)
                    {
                        break;
                    }

                    for(int i=0; i<nodeList.getLength(); i++)
                    {
                        Node node = nodeList.item(i);
                        if(node instanceof Element)
                        {
                            element = (Element) node;
                            NodeList nodeListEntry = element.getElementsByTagName("content");

                            Element elementContent = (Element) nodeListEntry.item(0);
                            NodeList listItems = elementContent.getElementsByTagName("items-desc");
                            String items = listItems.item(0).getTextContent();
                            listItems = elementContent.getElementsByTagName("filing-href");
                            String href = listItems.item(0).getTextContent();
                            listItems = elementContent.getElementsByTagName("filing-date");
                            String fileDate = listItems.item(0).getTextContent();

                            if(items.contains("2.05"))
                            {
                                System.out.println("has disposal");
                                MongoConnector.queryInsert("disposals", new BasicDBObject().append("content", 0)
                                        .append("cik", cik).append("href", href).append("fileDate", fileDate)
                                        .append("companyName", companyName), null);
                            }
                        }
                    }

                    currentCount += 100;

                }

                MongoConnector.queryUpdate("cik", new BasicDBObject().append("_id", _id), new BasicDBObject().append("status", 2), false, false, null);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }


    }
}
