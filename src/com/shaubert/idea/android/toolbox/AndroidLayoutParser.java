package com.shaubert.idea.android.toolbox;

import com.intellij.openapi.vfs.VirtualFile;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;

public class AndroidLayoutParser extends DefaultHandler {

    private AndroidView currentView;

    public AndroidView parse(VirtualFile virtualFile) {
        try {
            return parse(virtualFile.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            return new AndroidView();
        }
    }

    public AndroidView parse(InputStream inputStream) {
        this.currentView = new AndroidView();
        try {
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            SAXParser saxParser = saxParserFactory.newSAXParser();
            saxParser.parse(inputStream, this);
            return currentView;
        } catch (Exception e) {
            e.printStackTrace();
            return new AndroidView();
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
                currentView.addSubView(view);
                currentView = view;
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (currentView.getParent() != null
                && currentView.getTagName().equals(qName)) {
            currentView = currentView.getParent();
        }
    }
}
