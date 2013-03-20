package com.shaubert.idea.android.toolbox;

import com.intellij.openapi.vfs.VirtualFile;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AndroidLayoutParser extends DefaultHandler {

    private List<AndroidView> result = new ArrayList<AndroidView>();

    public List<AndroidView> parse(VirtualFile virtualFile) {
        result.clear();
        try {
            return parse(virtualFile.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<AndroidView> parse(InputStream inputStream) {
        result.clear();
        try {
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            SAXParser saxParser = saxParserFactory.newSAXParser();
            saxParser.parse(inputStream, this);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        AndroidView view = new AndroidView();
        view.setTagName(qName);
        String id = attributes.getValue("android:id");
        if (id != null && id.length() > 0) {
            int idStart = id.indexOf("/");
            if (idStart >= 0) {
                view.setIdValue(id.substring(idStart + 1));
                result.add(view);
            }
        }
    }
}
