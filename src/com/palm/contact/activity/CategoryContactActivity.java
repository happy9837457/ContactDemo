package com.palm.contact.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Data;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import com.palm.contact.R;
import com.palm.contact.util.StringUtil;
import com.palm.contact.util.ValidateUtil;

/**
 * 分组显示通讯录
 * 
 * @author weixiang.qin
 * 
 */
@SuppressLint("UseSparseArrays")
public class CategoryContactActivity extends Activity implements
		OnItemClickListener {
	private String[] projection;
	private String columnName;
	private Activity mActivity;
	private List<ContentValues> contacts;
	private ContactAdapter adapter;
	private ListView contactLv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_category);
		mActivity = this;
		if (Build.VERSION.SDK_INT < 19) {
			columnName = "sort_key";
		} else {
			columnName = "phonebook_label";
		}
		projection = new String[] { Data._ID, Phone.DISPLAY_NAME, Phone.NUMBER,
				columnName };
		contactLv = (ListView) findViewById(R.id.contact_lv);
		adapter = new ContactAdapter();
		contactLv.setAdapter(adapter);
		contactLv.setOnItemClickListener(this);
		getContact(mActivity, "", false);
	}

	private void getContact(Activity mActivity, String key, boolean isMobileNo) {
		ContentResolver resolver = mActivity.getContentResolver();
		Cursor cursor = resolver.query(Phone.CONTENT_URI, projection,
				Phone.NUMBER + " like '%" + key + "%' or " + Phone.DISPLAY_NAME
						+ " like '%" + key + "%'", null,
				"sort_key COLLATE LOCALIZED asc");
		if (cursor != null) {
			contacts = new ArrayList<ContentValues>();
			while (cursor.moveToNext()) {
				int id = cursor.getInt(0);
				String name = cursor.getString(1);
				String number = StringUtil.replaceSpace(cursor.getString(2));
				if (StringUtil.isEmpty(number)) {
					continue;
				}
				if (number.startsWith("+86")) {
					number = number.substring(3);
				}
				// 匹配手机号
				if (isMobileNo && !ValidateUtil.validateMobileNo(number)) {
					continue;
				}
				String sortKey = cursor.getString(3);
				ContentValues contentValues = new ContentValues();
				contentValues.put(Data._ID, id);
				contentValues.put(Phone.DISPLAY_NAME, name);
				contentValues.put(Phone.NUMBER, number);
				contentValues.put(columnName, sortKey);
				contacts.add(contentValues);
			}
			adapter.setContact(contacts);
			cursor.close();
		}
	}

	private class ContactAdapter extends BaseAdapter implements SectionIndexer {
		private List<ContentValues> list;
		private HashMap<String, Integer> alphaIndexer;// 保存每个索引在list中的位置【#-0，A-4，B-10】
		private String[] sections;// 每个分组的索引表【A,B,C,F...】

		public ContactAdapter() {
			list = new ArrayList<ContentValues>();
		}

		public void setContact(List<ContentValues> list) {
			this.list = list;
			this.alphaIndexer = new HashMap<String, Integer>();
			for (int i = 0; i < list.size(); i++) {
				String name = getAlpha(list.get(i).getAsString(columnName));
				if (!alphaIndexer.containsKey(name)) {// 只记录在list中首次出现的位置
					alphaIndexer.put(name, i);
				}
			}
			Set<String> sectionLetters = alphaIndexer.keySet();
			ArrayList<String> sectionList = new ArrayList<String>(
					sectionLetters);
			Collections.sort(sectionList);
			sections = new String[sectionList.size()];
			sectionList.toArray(sections);
			this.notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.bindData(position);
			return holder.view;
		}

		/*
		 * 此方法根据联系人的首字母返回在list中的位置
		 */
		@Override
		public int getPositionForSection(int section) {
			String later = sections[section];
			return alphaIndexer.get(later);
		}

		/*
		 * 本例中可以不考虑这个方法
		 */
		@Override
		public int getSectionForPosition(int position) {
			String key = getAlpha(list.get(position).getAsString(columnName));
			for (int i = 0; i < sections.length; i++) {
				if (sections[i].equals(key)) {
					return i;
				}
			}
			return 0;
		}

		@Override
		public Object[] getSections() {
			return sections;
		}
	}

	/**
	 * 提取英文的首字母，非英文字母用#代替
	 * 
	 * @param str
	 * @return
	 */
	private String getAlpha(String str) {
		if (str == null) {
			return "#";
		}
		if (str.trim().length() == 0) {
			return "#";
		}
		char c = str.trim().substring(0, 1).charAt(0);
		Pattern pattern = Pattern.compile("^[A-Za-z]+$");// 判断首字母是否是英文字母
		if (pattern.matcher(c + "").matches()) {
			return (c + "").toUpperCase(); // 大写输出
		} else {
			return "#";
		}
	}

	class ViewHolder {
		public View view;
		public TextView titleTv;
		public TextView nameTv;
		public TextView numberTv;

		public ViewHolder() {
			view = LayoutInflater.from(mActivity).inflate(
					R.layout.listview_contact_item, null);
			titleTv = (TextView) view.findViewById(R.id.title_tv);
			nameTv = (TextView) view.findViewById(R.id.name_tv);
			numberTv = (TextView) view.findViewById(R.id.number_tv);
			view.setTag(this);
		}

		public void bindData(int position) {
			ContentValues contentValues = contacts.get(position);
			// final int id = contentValues.getAsInteger(Data._ID);
			final String name = contentValues.getAsString(Phone.DISPLAY_NAME);
			final String number = contentValues.getAsString(Phone.NUMBER);
			nameTv.setText(name);
			numberTv.setText(number);
			// 当前联系人的sortKey
			String currentStr = getAlpha(contacts.get(position).getAsString(
					columnName));
			// 上一个联系人的sortKey
			String previewStr = (position - 1) >= 0 ? getAlpha(contacts.get(
					position - 1).getAsString(columnName)) : " ";
			if (!previewStr.equals(currentStr)) {
				titleTv.setVisibility(View.VISIBLE);
				titleTv.setText(currentStr);
			} else {
				titleTv.setVisibility(View.GONE);
			}
		}

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		ContentValues contentValues = (ContentValues) parent
				.getItemAtPosition(position);
		Toast.makeText(mActivity,
				"选择了:" + contentValues.getAsString(Phone.DISPLAY_NAME),
				Toast.LENGTH_SHORT).show();
	}
}
