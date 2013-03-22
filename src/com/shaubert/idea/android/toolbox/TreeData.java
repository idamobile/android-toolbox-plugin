package com.shaubert.idea.android.toolbox;

import java.util.List;

public interface TreeData {

    List<? extends TreeData> getChildNodes();

    TreeData getParent();

    String getNodeName();

}