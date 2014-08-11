package com.palm.contact.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.palm.contact.R;
import com.palm.contact.util.StringUtil;
import com.palm.contact.util.ValidateUtil;

/**
 * 获取手机通讯录 需要android.permission.READ_CONTACTS权限
 * 
 * @author weixiang.qin
 * 
 */
public class NormalContactActivity extends Activity implements
		OnItemClickListener {
	private final String[] PHONES_PROJECTION = new String[] {
			Phone.DISPLAY_NAME, Phone.NUMBER, "sort_key" };
	private Activity mActivity;
	private ContactAdapter adapter;
	private ListView contactLv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_normal);
		mActivity = this;
		contactLv = (ListView) findViewById(R.id.contact_lv);
		adapter = new ContactAdapter();
		contactLv.setAdapter(adapter);
		contactLv.setOnItemClickListener(this);
		getContact(mActivity, "", true);
	}

	/**
	 * 获取通讯录
	 * 
	 * @param mActivity
	 * @param key
	 * @param isMobileNo
	 */
	private void getContact(Activity mActivity, String key, boolean isMobileNo) {
		ContentResolver resolver = mActivity.getContentResolver();
		// 如果获取SIM卡通讯录 替换Phone.CONTENT_URI为Uri.parse("content://icc/adn")
		Cursor cursor = resolver.query(Phone.CONTENT_URI, PHONES_PROJECTION,
				Phone.NUMBER + " like '%" + key + "%' or " + Phone.DISPLAY_NAME
						+ " like '%" + key + "%'", null, "sort_key_alt asc");// sort_key_alt中文名称排序
		if (cursor != null) {
			List<Contact> contacts = new ArrayList<Contact>();
			while (cursor.moveToNext()) {
				String name = cursor.getString(0);
				String number = StringUtil.replaceSpace(cursor.getString(1));
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
				contacts.add(new Contact(name, number));
			}
			cursor.close();
			adapter.setContact(contacts);
		}
	}

	class ContactAdapter extends BaseAdapter {
		private List<Contact> contacts;

		public ContactAdapter() {
			contacts = new ArrayList<Contact>();
		}

		public void setContact(List<Contact> contacts) {
			this.contacts = contacts;
			this.notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return contacts.size();
		}

		@Override
		public Object getItem(int position) {
			return contacts.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder = null;
			if (convertView == null) {
				viewHolder = new ViewHolder();
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			viewHolder.bindData(contacts.get(position));
			return viewHolder.view;
		}

	}

	class Contact {
		public String name;
		public String number;

		public Contact(String name, String number) {
			this.name = name;
			this.number = number;
		}

	}

	class ViewHolder {
		public View view;
		public TextView nameTv;
		public TextView numberTv;

		public ViewHolder() {
			view = LayoutInflater.from(mActivity).inflate(
					R.layout.listview_contact_item, null);
			nameTv = (TextView) view.findViewById(R.id.name_tv);
			numberTv = (TextView) view.findViewById(R.id.number_tv);
			view.setTag(this);
		}

		public void bindData(Contact contact) {
			nameTv.setText(contact.name);
			numberTv.setText(contact.number);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Contact contact = (Contact) parent.getItemAtPosition(position);
		Toast.makeText(mActivity, "选择了:" + contact.name, Toast.LENGTH_SHORT)
				.show();
	}
}
