Android Toolbox Plugin
======================

Android developement speed up plugin for IDEA

Features:
* Generate ViewHolder or ViewPresenter from layout xml


Generating ViewHolder/ViewPresenter templates
---

1. Suppose we have an android layout file (l_dirty_comment.xml)

        <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
            android:id="@+id/message_frame"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content">
        
            <LinearLayout
                android:id="@+id/frame_body"
                android:layout_width="fill_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical">
        
                <com.shaubert.widget.PatchedTextView
                    android:id="@+id/message"
                    android:layout_width="fill_parent"
                    android:layout_height="wrap_content"/>
        
                <RelativeLayout
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content">
        
                    <com.shaubert.util.FixedSizeImageView
                        android:id="@+id/image"
                        android:layout_width="128dp"
                        android:layout_height="128dp"/>
        
                    <TextView
                        android:id="@+id/gif_description"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"/>
                </RelativeLayout>
        
                <com.shaubert.widget.PatchedTextView
                    android:id="@+id/summary"
                    android:layout_width="wrap_content"/>
        
            </LinearLayout>
        </LinearLayout>
 
2. Select Android layout file in res/layout*/ folder. Open context menu for it and select "Generate View Presenter…" option:

![Context menu](../screenshots/context_menu.png?raw=true)

3. Select generation pattern:

![Pattert selection dialog](../screenshots/select_pattern.png?raw=true)

4. Choose views:

![View selection dialog](../screenshots/view_selection.png?raw=true)

5. Select generated class' package:

![Package selection dialog](../screenshots/select_package.png?raw=true)

6. Modify generated class name:

![Class name Input dialog](../screenshots/select_class_name.png?raw=true)

7. Browse the result code:

        public class DirtyCommentHolder {
            private LinearLayout messageFrame;
            private PatchedTextView message;
            private FixedSizeImageView image;
            private PatchedTextView summary;
        
            public DirtyCommentHolder(View view) {
                this.messageFrame = (LinearLayout) view.findViewById(R.id.message_frame);
                this.message = (PatchedTextView) messageFrame.findViewById(R.id.message);
                this.image = (FixedSizeImageView) messageFrame.findViewById(R.id.image);
                this.summary = (PatchedTextView) messageFrame.findViewById(R.id.summary);
            }

            public LinearLayout getMessageFrame() {
                return messageFrame;
            }

            public PatchedTextView getMessage() {
                return message;
            }
        
            public FixedSizeImageView getImage() {
                return image;
            }
        
            public PatchedTextView getSummary() {
                return summary;
            }
        }
        
For View Presenter Pattert the result code would be:

        public class LDirtyCommentPresenter {
            private LinearLayout messageFrame;
            private PatchedTextView message;
            private FixedSizeImageView image;
            private PatchedTextView summary;
            private View view;
            private DirtyBlog data;
        
            public LDirtyCommentPresenter(Context context, ViewGroup parent) {
                LayoutInflater inflater = LayoutInflater.from(context);
                this.view = inflater.inflate(R.layout.l_dirty_comment, parent, false);
                this.messageFrame = (LinearLayout) view.findViewById(R.id.message_frame);
                this.message = (PatchedTextView) messageFrame.findViewById(R.id.message);
                this.image = (FixedSizeImageView) messageFrame.findViewById(R.id.image);
                this.summary = (PatchedTextView) messageFrame.findViewById(R.id.summary);
            }
        
            public View getView() {
                return view;
            }
        
            public DirtyBlog getData() {
                return data;
            }
        
            public void refresh() {
                if (this.data != null) {
                    this.view.setVisibility(View.VISIBLE);
                } else {
                    this.view.setVisibility(View.GONE);
                }
            }
        
            public void swapData(DirtyBlog data) {
                if (this.data != data) {
                    this.data = data;
                    refresh();
                }
            }
        }        
        
  Licence
  ==
  
             Copyright 2013 iDa Mobile.
        
           Licensed under the Apache License, Version 2.0 (the "License");
           you may not use this file except in compliance with the License.
           You may obtain a copy of the License at
        
               http://www.apache.org/licenses/LICENSE-2.0
        
           Unless required by applicable law or agreed to in writing, software
           distributed under the License is distributed on an "AS IS" BASIS,
           WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
           See the License for the specific language governing permissions and
           limitations under the License.