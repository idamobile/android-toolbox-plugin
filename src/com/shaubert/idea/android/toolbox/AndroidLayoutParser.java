package com.shaubert.idea.android.toolbox;

import com.intellij.openapi.vfs.VirtualFile;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

public class AndroidLayoutParser extends DefaultHandler {

    private AndroidView currentView;
    private int currentViewLevel;
    private Stack<Integer> prevLevels = new Stack<Integer>();
    private int level;

    public AndroidView parse(VirtualFile virtualFile) {
        try {
            return parse(virtualFile.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            return new AndroidView();
        }
    }

    public AndroidView parse(InputStream inputStream) {
        currentViewLevel = 1;
        level = 1;
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
        level++;
        if ("include".equals(qName)) {
            return;
        }
        if ("view".equals(qName)) {
            String className = attributes.getValue("class");
            if (className == null) {
                return;
            }
            qName = className;
        }
        String id = attributes.getValue("android:id");
        if (id != null && id.length() > 0) {
            int idStart = id.indexOf("/");
            if (idStart >= 0) {
                AndroidView view = new AndroidView();
                view.setTagName(qName);
                view.setIdValue(id.substring(idStart + 1));
                currentView.addSubView(view);
                currentView = view;
                prevLevels.add(currentViewLevel);
                currentViewLevel = level;
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        level--;
        if (currentView.getParent() != null
                && level < currentViewLevel) {
            currentViewLevel = prevLevels.pop();
            currentView = currentView.getParent();
        }
    }
}
