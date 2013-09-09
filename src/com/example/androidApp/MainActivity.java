package com.example.androidapp;

/*��������apache.http������������HTTP����*/  
import org.apache.http.HttpResponse;      
import org.apache.http.client.ClientProtocolException;   
import org.apache.http.client.methods.HttpGet;   
import org.apache.http.impl.client.DefaultHttpClient;   
import org.apache.http.util.EntityUtils;   

import java.util.Locale;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MainActivity extends FragmentActivity implements
     ActionBar.TabListener{

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	static Spanned strWebData;
	
	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;
		 
	Handler handler = new Handler() {
		  @Override
		  public void handleMessage(Message msg) {
		  Bundle bundle = msg.getData();
		  strWebData = Html.fromHtml(bundle.getString("webData"));
		  
		  switch(getActionBar().getSelectedNavigationIndex()) {
		  case 0:
			  TextView dummyTextView = (TextView)findViewById(R.id.section_label);
			  dummyTextView.setText(strWebData); 
			  break;
		  }
	     }
	 };
 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
		
		new Thread(UpdateRun).start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}
	
	
	/** 
	 * �����߳�
	 */  
	Runnable UpdateRun = new Runnable(){ 
		
		@Override  
		public void run() {  
			
			/*������ַ�ַ���*/  
		    String uriAPI = "http://ljlon.com";   
		    
		    /*����HTTP Get����*/  
		    HttpGet httpRequest = new HttpGet(uriAPI);   
		    try   
		    {   
		      /*����HTTP request*/  
		      HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);   
		      /*��״̬��Ϊ200 ok*/  
		      if(httpResponse.getStatusLine().getStatusCode() == 200)    
		      {   
		          /*ȡ����Ӧ�ַ���*/  
		          String strResult = EntityUtils.toString(httpResponse.getEntity());  
		          /*ɾ�������ַ�*/  
		          //strResult = eregi_replace("(/r/n|/r|/n|/n/r)","",strResult);  
		          Message msg = handler.obtainMessage();
	    		  Bundle bundle = new Bundle();
	    		  bundle.putString("webData", strResult);
	              msg.setData(bundle);
	              handler.sendMessage(msg);  
		      }   
		      else   
		      {   
		    	  //textView.setText("Error Response: "+httpResponse.getStatusLine().toString());   
		      }   
	        }   
	        catch (ClientProtocolException e)   
	        {    
	        	e.printStackTrace();  
	        }    
	        catch (Exception e)   
	        {     
	        	e.printStackTrace();  
	        }  
		}  
	}; 
	

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.
			Fragment fragment = new DummySectionFragment();
			Bundle args = new Bundle();
			args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			case 2:
				return getString(R.string.title_section3).toUpperCase(l);
			}
			return null;
		}
	}

	
	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class DummySectionFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_SECTION_NUMBER = "section_number";

		public DummySectionFragment() {
		}
	   
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main_dummy,
					container, false);
			TextView dummyTextView = (TextView)rootView.findViewById(R.id.section_label);
			dummyTextView.setMovementMethod(ScrollingMovementMethod.getInstance());
			
			switch(getArguments().getInt(ARG_SECTION_NUMBER)) {
			case 1:
				dummyTextView.setText(strWebData); 
				break;
			case 2:
				dummyTextView.setText(getString(R.string.title_section2)); 
				break;
			case 3:
				dummyTextView.setText(getString(R.string.title_section3)); 
				break;
			}
			
			return rootView;
		} 
		
	}

}
