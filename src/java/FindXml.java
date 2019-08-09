package java;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 对App的词条管理/比较/校对
 *
 * @author Hongzhi Liu
 * @date 2018/11/26 9:41
 */
public class FindXml {

    private Map<String, String> beforeXml = new HashMap<>();


    private static final String xmlFileName = "strings-nl.xml";
    private static final String newXmlFileName = "strings1-zh-cn.xml";
    private static final String latestXmlFileName = "strings2-zh-cn.xml";


    public static void main(String[] args) {
        FindXml findXml = new FindXml();

//        findXml.compareXml(xmlFileName, newXmlFileName);

        findXml.compareWithXml2("strings.xml", "strings2.xml");

//        File inputExcel = new File("excel", "resource.xlsx");
//        if (!inputExcel.exists()) {
//            System.out.println("excel is not exist");
//        }
//        try {
//            readExcelToXml(inputExcel);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    }

    private void compareXml(String beforeXmlFileName, String xmlFileName) {
        readBeforeXml(beforeXmlFileName);
        compareWithXml(xmlFileName);

    }

    private static File readExcelToXml(File excelFile) throws Exception {
        Document document = DocumentHelper.createDocument();
        Element rootElement = document.addElement("resources");
        //read from column 3 and 15, and in range of line 2 to end
        final int keyColumn = 2;
        final int langColumn = 14;
        FileInputStream fileInputStream = new FileInputStream(excelFile);
        XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);
        //读取excel里的第0张表
        XSSFSheet sheet = workbook.getSheetAt(0);
        System.out.println("sheet last row num:  " + sheet.getLastRowNum());
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            XSSFRow row = sheet.getRow(i);
            String key = row.getCell(keyColumn).getStringCellValue();
            String word = row.getCell(langColumn).getStringCellValue();
            if (null == key || key.equals("") || null == word || word.equals("")) {
                continue;
            }
            Element element = rootElement.addElement("string");
            element.addAttribute("name", key);
            element.setText(word);
        }
        fileInputStream.close();


        //开始输出
        File result = new File("generatedXml", "strings2.xml");
        if (result.exists()) {
            result.delete();
        }
        result.createNewFile();
        //用于格式化xml内容和设置头部标签
        OutputFormat format = OutputFormat.createPrettyPrint();
        //设置xml文档的编码为utf-8
        format.setEncoding("utf-8");
        try {
            FileWriter fileWriter = new FileWriter(result, true);
            //创建一个dom4j创建xml的对象
            XMLWriter writer = new XMLWriter(fileWriter, format);
            writer.write(document);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private void readBeforeXml(String xmlFileName) {
        SAXReader saxReader = new SAXReader();
        File beforeXmlFile = new File("xmlFile", xmlFileName);
        try {
            Document document = saxReader.read(beforeXmlFile);
            Element resourceElement = document.getRootElement();
            Iterator it = resourceElement.elementIterator();
            while (it.hasNext()) {
                Element string = (Element) it.next();
                String stringId = string.attributeValue("name");
                String stringValue = string.getStringValue();
                beforeXml.put(stringId, stringValue);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void compareWithXml(String xmlFileName) {
        SAXReader saxReader = new SAXReader();
        File beforeXmlFile = new File("xmlFile", xmlFileName);
        HashMap<String, String> generateXmlFile = new HashMap<>();
        try {
            Document document = saxReader.read(beforeXmlFile);
            Element resourceElement = document.getRootElement();
            Iterator it = resourceElement.elementIterator();
            System.out.println("--------------add string:---------------------");
            while (it.hasNext()) {
                Element string = (Element) it.next();
                String stringId = string.attributeValue("name");
                String stringValue = string.getStringValue();
                if (beforeXml.get(stringId) == null) {
                    System.out.println("<string name = \"" + stringId + "\">" + stringValue + "</string>");
                    generateXmlFile.put(stringId, stringValue);
                }
            }
            System.out.println("--------------add string Finished---------------------");
            System.out.println("\n\n\n\n");
            Iterator it2 = resourceElement.elementIterator();
            System.out.println("--------------modify string:---------------------");
            while (it2.hasNext()) {
                Element string = (Element) it2.next();
                String stringId = string.attributeValue("name");
                String stringValue = string.getStringValue();
                if (beforeXml.get(stringId) != null) {
                    String beforeStringValue = beforeXml.get(stringId);
                    if (!beforeStringValue.equals(stringValue)) {
                        System.out.println(stringId + ":         " + beforeStringValue + "               " + stringValue);
                    }
                }
            }
            System.out.println("--------------modify string Finished---------------------");
        } catch (Exception e) {
            e.printStackTrace();
        }
        generateXmlFile(generateXmlFile, xmlFileName);
    }


    /**
     * 将第二个Xml在第一个Xml基础上修改和新增的输出到xml文件中
     *
     * @param xmlFileName1
     * @param xmlFileName2
     */
    private void compareWithXml2(String xmlFileName1, String xmlFileName2) {
        Map<String, String> beforeElements = new HashMap<>();
        SAXReader saxReader = new SAXReader();
        File beforeXmlFile = new File("xmlFile", xmlFileName1);
        File latestXmlFile = new File("xmlFile", xmlFileName2);
        HashMap<String, String> generateXmlFile = new HashMap<>();
        try {
            Document document = saxReader.read(beforeXmlFile);
            Element resourceElement = document.getRootElement();
            Iterator it = resourceElement.elementIterator();
            while (it.hasNext()) {
                Element string = (Element) it.next();
                String stringId = string.attributeValue("name");
                String stringValue = string.getStringValue();
                beforeElements.put(stringId, stringValue);
            }
            Document document2 = saxReader.read(latestXmlFile);
            Element resourceElement2 = document2.getRootElement();
            Iterator it2 = resourceElement2.elementIterator();
            while (it2.hasNext()) {
                Element string = (Element) it2.next();
                String stringId = string.attributeValue("name");
                String stringValue = string.getStringValue();
                if (null == beforeElements.get(stringId)) {
                    generateXmlFile.put(stringId, stringValue);
                    System.out.println("--------add string:      " + stringId + "          " + stringValue);
                } else if (!beforeElements.get(stringId).equals(stringValue)) {
                    generateXmlFile.put(stringId, stringValue);
                    System.out.println("--------modify string:      " + stringId + "          " + beforeElements.get(stringId) + "          " + stringValue);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
//        generateXmlFile(generateXmlFile, xmlFileName2);
    }


    private void generateXmlFile(HashMap<String, String> xmlElements, String fileName) {
        Document document = DocumentHelper.createDocument();
        Element rootElement = document.addElement("resources");
        for (Map.Entry<String, String> elementEntry : xmlElements.entrySet()) {
            //过滤undefined词条
            if (!elementEntry.getValue().equals("undefined")) {
                Element stringElement = rootElement.addElement("string");
                stringElement.addAttribute("name", elementEntry.getKey());
                stringElement.setText(elementEntry.getValue());
            }
        }
        File newXmlFile = new File("generatedXml", fileName);
        //用于格式化xml内容和设置头部标签
        OutputFormat format = OutputFormat.createPrettyPrint();
        //设置xml文档的编码为utf-8
        format.setEncoding("utf-8");
        try {
            FileWriter fileWriter = new FileWriter(newXmlFile, true);
            //创建一个dom4j创建xml的对象
            XMLWriter writer = new XMLWriter(fileWriter, format);
            writer.write(document);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
