/*
 * Copyright (c) 2015 张涛.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kymjs.contacts;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kymjs.contacts.adapter.ContactAdapter;
import org.kymjs.contacts.bean.Contact;
import org.kymjs.contacts.util.ConstantSet;
import org.kymjs.contacts.util.HanziToPinyin;
import org.kymjs.contacts.widget.SideBar;
import org.kymjs.kjframe.KJActivity;
import org.kymjs.kjframe.KJHttp;
import org.kymjs.kjframe.http.HttpCallBack;
import org.kymjs.kjframe.http.HttpConfig;
import org.kymjs.kjframe.ui.BindView;
import org.kymjs.kjframe.utils.KJLoger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * 用户联系人列表
 *
 * @author kymjs (http://www.kymjs.com/) on 7/24/15.
 */
public class MainActivity extends KJActivity implements SideBar
        .OnTouchingLetterChangedListener, TextWatcher,
        ContactAdapter.OnEditAddListListener {

    @BindView(id = R.id.school_friend_member)
    private ListView mListView;
    private TextView mFooterView,comment_next;

    private RelativeLayout layout_choose_all;
    private CheckBox chooseAll;

    private KJHttp kjh = null;
    private ArrayList<Contact> datas = new ArrayList<>();
    private ContactAdapter mAdapter;
    protected HashSet<Contact> selectDataList = new HashSet<Contact>();

    @Override
    public void setRootView() {
        setContentView(R.layout.activity_main);
    }

    @Override
    public void initData() {
        super.initData();
        HttpConfig config = new HttpConfig();
        HttpConfig.sCookie = "oscid=V" +
                "%2BbmxZFR8UfmpvrBHAcVRKALrd72iPWknXaWDa5Is3veK7WsxTSWY80kRXB1LoH%2F4VJ" +
                "%2F9s7K3Kd9CwCC1CAV%2BnJ7T3ka0blF8vZojozhUdOYkq6D6Laldg%3D%3D; Domain=.oschina" +
                ".net; Path=/; ";
        config.cacheTime = 0;
        kjh = new KJHttp();
    }

    @Override
    public void initWidget() {
        super.initWidget();
        SideBar mSideBar = (SideBar) findViewById(R.id.school_friend_sidrbar);
        TextView mDialog = (TextView) findViewById(R.id.school_friend_dialog);
        EditText mSearchInput = (EditText) findViewById(R.id.school_friend_member_search_input);

        mSideBar.setTextView(mDialog);
        mSideBar.setOnTouchingLetterChangedListener(this);
        mSearchInput.addTextChangedListener(this);

        comment_next = (TextView) findViewById(R.id.comment_next);
        mFooterView = (TextView) View.inflate(aty, R.layout.item_list_contact_count, null);
        mListView.addFooterView(mFooterView);


        layout_choose_all = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.layout_choose_all, null);

        chooseAll = (CheckBox) layout_choose_all.findViewById(R.id.select_all);
        mListView.addHeaderView(layout_choose_all);

//        doHttp();
        addListener();
        parser(ConstantSet.testdata);
    }

    private void doHttp() {
        kjh.get("http://zb.oschina.net/action/zbApi/contacts_list?uid=863548", new HttpCallBack() {
            @Override
            public void onSuccess(String t) {
                super.onSuccess(t);
                parser(t);
            }
        });
    }

    private void addListener() {
        comment_next.setOnClickListener(this);
        chooseAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    for (int i = 0; i < datas.size(); i++) {
                        datas.get(i).setSelected(true);
                    }
                    selectDataList.addAll(datas);
                } else {
                    for (int i = 0; i < datas.size(); i++) {
                        datas.get(i).setSelected(false);
                    }
                    selectDataList.clear();
                }
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private void parser(String json) {
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.optJSONObject(i);
                Contact data = new Contact();
                data.setName(object.optString("name"));
                data.setUrl(object.optString("portrait"));
                data.setId(object.optInt("id"));
                data.setPinyin(HanziToPinyin.getPinYin(data.getName()));
                datas.add(data);
            }
            mFooterView.setText(datas.size() + "位联系人");
            mAdapter = new ContactAdapter(mListView, datas,this);
            mListView.setAdapter(mAdapter);
        } catch (JSONException e) {
            KJLoger.debug("解析异常" + e.getMessage());
        }
    }

    @Override
    public void editAddList(int position, boolean isAdd) {
        if (isAdd) {
            selectDataList.add(datas.get(position));
        } else {
            selectDataList.remove(datas.get(position));
        }
    }

    @Override
    public void onTouchingLetterChanged(String s) {
        int position = 0;
        // 该字母首次出现的位置
        if (mAdapter != null) {
            position = mAdapter.getPositionForSection(s.charAt(0));
        }
        if (position != -1) {
            mListView.setSelection(position);
        } else if (s.contains("#")) {
            mListView.setSelection(0);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        ArrayList<Contact> temp = new ArrayList<>(datas);
        for (Contact data : datas) {
            if (data.getName().contains(s) || data.getPinyin().contains(s)) {
            } else {
                temp.remove(data);
            }
        }
        if (mAdapter != null) {
            mAdapter.refresh(temp);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if(comment_next == v){

            if(selectDataList.size()>0){
                ArrayList<Contact> selectedMembers=new ArrayList<Contact>();
                Iterator<Contact> iterator= selectDataList.iterator();
                while(iterator.hasNext()){
                    selectedMembers.add(iterator.next());
                }
            }

//                Intent intent=new Intent(this,CommentAddCommentActivity.class);
//                intent.putExtra(Constants.SELECTED_STUDENTS,selectedMembers);
//                startActivity(intent);
        }
    }
}
