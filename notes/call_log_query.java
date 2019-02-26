//https://stackoverflow.com/questions/7237655/android-querying-call-log-after-it-has-been-updated-once-a-call-ends
//Here is complete example

//This is PhoneStateListener class

//
public class CustomPhoneStateListener extends PhoneStateListener {

//variable(s) (initiialize)
Context context;

//constructor
public CustomPhoneStateListener(Context context) { //takes in a context argument
    super(); //Calls PhoneStateListener constructor
    this.context = context; //turns the CustomPhoneStateListener context into the passed context
}

//function(s)
@Override
public void onCallStateChanged(int state, String incomingNumber) { //event handler (mutator)

super.onCallStateChanged(state, incomingNumber); //call super class first

switch (state) {
    case TelephonyManager.CALL_STATE_IDLE: //when Idle i.e no call
        // Toast.makeText(context, "CALL_STATE_IDLE", Toast.LENGTH_LONG).show();
        if(UDF.phoneState != TelephonyManager.CALL_STATE_IDLE) { //WTF is UDF?
            UDF.fetchNewCallLogs(context);
        } 
        break;
    case TelephonyManager.CALL_STATE_OFFHOOK: //when Off hook i.e in call
        ////Make intent and start your service here
         //Toast.makeText(context, "CALL_STATE_OFFHOOK", Toast.LENGTH_LONG).show();
        break;
    case TelephonyManager.CALL_STATE_RINGING: //when Ringing
         //Toast.makeText(context, "CALL_STATE_RINGING", Toast.LENGTH_LONG).show();
        endCallIfBlocked(incomingNumber);
        break;

    default:
        break;
}
UDF.phoneState = state;
}

//This is broadcast receiver class

public class PhoneStateBroadcastReceiver extends BroadcastReceiver{

    //(the only added) function(s)
    @Override
    //event handler (listener) (mutator)
    public void onReceive(Context context, Intent intent) { //passes in context (from where?)
        //UDF.createTablesIfNotExists(context);
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE); //uses a constant system service within a context class and turns it into a Telephony Manager
        telephonyManager.listen(new CustomPhoneStateListener(context), PhoneStateListener.LISTEN_CALL_STATE); 
        //Registers a listener object to receive notification of changes in specified telephony states.
        //CPSL is registered and will get callbacks whenever some change is observed in TELEPHONY_SERVICE
        //2nd arg directs obj to listen for onCallStateChanged(int state, String incomingNumber) - integer
    }
}

//This is function for get new call logs from internal database

public static void fetchNewCallLogs(Context context) { //what context should be passed into this?

        CallLogHelper callLogHelper = new CallLogHelper(context);
        callLogHelper.open(); //opens callLogHelper (cLH)
        Long maxId = callLogHelper.getMaxId(); //gets the highest Id from the cLH
        //I will need the MaxId from a specific user within the callLog

        Cursor c = context.getContentResolver().query(android.provider.CallLog.Calls.CONTENT_URI, null, "_id > ?", new String[]{String.valueOf(maxId)}, null);
        //gets a Content Resolver from context
        //queries the Content Resolver from the context
            //1) android.provider.CallLog.Calls.CONTENT_URI, 
            //2) null, 
            //3) "_id > ?", 
            //4) new String[]{String.valueOf(maxId)}, 
            //5) null
        
        if(c != null && c.moveToFirst()) {
            while (c.isAfterLast() == false) {
                int _ID = c.getColumnIndex(android.provider.CallLog.Calls._ID);
                int _NUMBER = c.getColumnIndex(android.provider.CallLog.Calls.NUMBER);
                int _DATE =  c.getColumnIndex(android.provider.CallLog.Calls.DATE);
                int _DURATION =  c.getColumnIndex(android.provider.CallLog.Calls.DURATION);
                int _CALLTYPE =  c.getColumnIndex(android.provider.CallLog.Calls.TYPE);
                int _NAME =  c.getColumnIndex(android.provider.CallLog.Calls.CACHED_NAME);
                int _NUMBERTYPE =  c.getColumnIndex(android.provider.CallLog.Calls.CACHED_NUMBER_TYPE);
                int _NEW = c.getColumnIndex(android.provider.CallLog.Calls.NEW);

                String id = c.getString(_ID);
                String number = c.getString(_NUMBER);
                String date = c.getString(_DATE);
                String duration = c.getString(_DURATION);
                String callType = c.getString(_CALLTYPE);
                String name = c.getString(_NAME);
                String numberType = c.getString(_NUMBERTYPE);
                String _new = c.getString(_NEW);

                callLogHelper.createLog(id, number, date, duration, callType, name, numberType, _new, "N");

                c.moveToNext();
            }
        }

        callLogHelper.close(); //closes callLogHelper (cLH)
    }

     public boolean onKeyDown(int keyCode, KeyEvent event) {
         if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
             // When the user center presses, let them pick a contact.
             startActivityForResult(
                 new Intent( Intent.ACTION_PICK, new Uri("content://contacts") ), PICK_CONTACT_REQUEST
                 );
            return true;
         }

/*
**Where** 


 => CallLogHelper is a helper class to communicate with my local database
    => callLogHelper.getMaxId(); will returns the maximum id of call logs in my local database and I am keeping the id in local database and internal database will be same
    => callLogHelper.createLog() is my function to insert call log in my local database
*/