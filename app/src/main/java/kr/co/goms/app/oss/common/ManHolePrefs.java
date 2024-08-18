package kr.co.goms.app.oss.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;

import kr.co.goms.app.oss.MyApplication;
import kr.co.goms.module.common.util.Utils;

/**
 * SharedPreferences는 데이터를 앱 폴더에 저장하여 관리.
 * 앱이 삭제되면 데이터 파일도 삭제됨
 */
public class ManHolePrefs {

	public static final String EMPTY							= "";
	public static final String S_KEY							= "s_key";				//AES_KEY
	//최초 1회 앱을 실행시켰는지 여부
	public static final String IS_FIRST_TIME_EXECUTE_THE_APP	= "is_first_time_execute_the_app";
	public static final String MH_BLOCK_IMPORT_YN			= "mh_block_import_yn";	//블록이음부 포함 여부 확인 > 추후 설정에서 추가할 것
	public static final String MB_IDX						= "";
	public static final String MB_PRELOAD_DATA				= "preload_data";	//이전 저장 데이타 불러오기

	private static ManHolePrefs mManHolePrefs				= null;					//인스턴스
	private SharedPreferences  mPrefs;												//SharedPreferences

	private ManHolePrefs(Context context) {
		mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
	}

	/**
	 * 최초 context는 반드시 필요함.
	 * @param context
	 * @return
	 */
	public static ManHolePrefs getInstance(Context context) {
		if (mManHolePrefs == null)
			mManHolePrefs = new ManHolePrefs(context);
		return mManHolePrefs;
	}

	/**
	 * 이후 부터는 이 메소드를 이용하여 싱글턴으로 객체를 사용.
	 * 
	 * @return
	 */
	public static ManHolePrefs getInstance() {
		return mManHolePrefs;
	}

	public SharedPreferences getPrefs()
	{
		return mPrefs;
	}

	/**
	 * Default
	 * 
	 * @param pKey
	 * @return
	 */
	public String get(String pKey) {
		try {
			return get(pKey, EMPTY);
		} catch (Exception e) {
			return EMPTY;
		}
	}

	public boolean del(String pKey)
	{
		return mPrefs.edit().remove(pKey).commit();
	}

	/****************************************************************
	 * boolean
	 ****************************************************************/
	public boolean put(String pKey, boolean pDefault)
	{
		return put(pKey, String.valueOf(pDefault));
	}

	public boolean get(String pKey, boolean pDefault) {
		try {
			return Boolean.valueOf(get(pKey, String.valueOf(pDefault)));
		} catch (Exception e) {
			return false;
		}
	}

	/******************************************************************
	 * int
	 ****************************************************************/
	public boolean put(String pKey, int pDefault)
	{
		return put(pKey, String.valueOf(pDefault));
	}

	public int get(String pKey, int pDefault) {
		try {
			return Integer.valueOf(get(pKey, String.valueOf(pDefault)));
		} catch (Exception e) {
			return 0;
		}
	}
	
	/****************************************************************
	 * long
	 ****************************************************************/
	public boolean put(String pKey, long pDefault)
	{
		return put(pKey, String.valueOf(pDefault));
	}

	public long get(String pKey, long pDefault) {
		try {
			return Long.valueOf(get(pKey, String.valueOf(pDefault)));
		} catch (Exception e) {
			return 0;
		}
	}

	/****************************************************************
	 * String
	 ****************************************************************/
	public boolean put(String pKey, String pValue) {
		return mPrefs.edit().putString(pKey, Utils.encrypt(pValue, MyApplication.mGomsJNI.ivKey(), MyApplication.getInstance().encryptKey())).commit();
	}

	public String get(String pKey, String pDefault) {
		String result = mPrefs.getString(pKey, pDefault);
		if (result == null || result.equals(pDefault)) return result;
		return Utils.decrypt(result, MyApplication.mGomsJNI.ivKey(), MyApplication.getInstance().encryptKey());
	}

	public boolean prefsRemove(String key) {
		SharedPreferences.Editor nEditor= mPrefs.edit();
		nEditor.remove(key);
		return nEditor.commit();
	}
	
	public boolean putList(String pKey, ArrayList<String> vList) {
		SharedPreferences.Editor nEditor= mPrefs.edit();
		nEditor.putInt(pKey, vList.size()); /*sKey is an array*/ 

		for (int i = 0; i < vList.size(); i++) {
			nEditor.remove(pKey + "_" + i);
			nEditor.putString(pKey + "_" + i, vList.get(i));  
		}
		return nEditor.commit();
	}
	
	public ArrayList<String> getList(String pKey) {
		ArrayList<String> stringPath = new ArrayList<>();
		
	    int size = mPrefs.getInt(pKey, 0);  
	    for (int i = 0; i < size; i++) {
	        stringPath.add(mPrefs.getString(pKey + "_" + i, null));  
	    }
	    return stringPath;
	}


	/******************************************************************
	 * for AES_KEY Save
	 ****************************************************************/
	public boolean putKey(String pValue) {
		return mPrefs.edit().putString(ManHolePrefs.S_KEY, pValue).commit();
	}
	public String getKey(){
		return mPrefs.getString(ManHolePrefs.S_KEY, EMPTY);
	}

}
