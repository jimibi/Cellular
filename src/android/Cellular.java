package fr.edps.cordova.cellular;

import java.util.List;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.telephony.gsm.GsmCellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;



public class Cellular extends CordovaPlugin {

    GsmCellLocation cellLocation;
    public static int cellID;       // cell Id
    public static int lac;          // location area code
    public static int psc;          // On a UMTS network, returns the primary scrambling code of the serving cell.

    MyPhoneStateListener MyListener;
    public static String imei;
    public static String operator;
    private static List<NeighboringCellInfo> NeighboringList;

    TelephonyManager Tel;

    /**
     * Constructor.
     */
    public Cellular() {
    }

    /**
     * Sets the context of the Command. This can then be used to do things like
     * get file paths associated with the Activity.
     *
     * @param cordova The context of the main Activity.
     * @param webView The CordovaWebView Cordova is running in.
     */
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        /* Get the telephony service from the native code, the context will be the cordova application
         * that is invoking the service
         */
        this.Tel = (TelephonyManager) cordova.getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        this.cellLocation = (GsmCellLocation) Tel.getCellLocation();
    }
    
    /**
     * Executes the request and returns PluginResult.
     *
     * @param action            The action to execute.
     * @param args              JSONArry of arguments for the plugin.
     * @param callbackContext   The callback id used when calling back into JavaScript.
     * @return                  True if the action was valid, false if not.
     */
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("getCellularInfo")) {
            JSONObject r = new JSONObject();
            r.put("cellID", this.getCellID());
            r.put("lac", this.getLac());
            r.put("psc", this.getPsc());
            r.put("imei", this.getMobileIdentifier());
            r.put("operator", this.getOperator());
            r.put("neighbors", this.getNeighbours());
            callbackContext.success(r);
        }
        else {
            return false;
        }
        return true;
    }
    
    //--------------------------------------------------------------------------
    // LOCAL METHODS
    //--------------------------------------------------------------------------
    /*
     * 1. getCellID()
     * 2. getLac()
     * 3. getPsc()
     * 4. getMobileIdentifier()
     * 5. getOperator()
     * 6. getNeighbours()
     */
    
    public int getCellID(){
        int cellId = this.cellLocation.getCid();
        return cellId;
    }    
    
    public int getLac(){
        int lac = this.cellLocation.getLac();
        return lac;
    }

    public int getPsc(){
        int psc = this.cellLocation.getLac();
        return psc;
    }

    public String getMobileIdentifier(){
        String deviceid = this.Tel.getDeviceId();
        return deviceid;
    }
    
    public String getOperator(){
        String operator = this.Tel.getNetworkOperatorName();
        return operator;
    }

    public JSONArray getNeighbours(){
        this.setNeighboringList(this.Tel.getNeighboringCellInfo());
        JSONArray neighborsArray = new JSONArray();
        for(int i=0; i< getNeighboringList().size(); i++){
            JSONObject neighbors = new JSONObject();
            String dbM = null,
                   cid = null,
                   lac = null,
                   psc = null;
            cid = String.valueOf(getNeighboringList().get(i).getCid());
            lac = String.valueOf(getNeighboringList().get(i).getLac());
            psc = String.valueOf(getNeighboringList().get(i).getPsc());
            int rssi = getNeighboringList().get(i).getRssi();
            if(rssi == NeighboringCellInfo.UNKNOWN_RSSI)
                dbM = "Unknown RSSI";
            else
                dbM = String.valueOf(-113 + 2 * rssi) + " dBm";
            try {
                neighbors.put("rssi", dbM);
                neighbors.put("cid", cid);
                neighbors.put("lac", lac);
                neighbors.put("psc", psc);
                neighborsArray.put(neighbors);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return neighborsArray;
    }
    

    public static List<NeighboringCellInfo> getNeighboringList() {
        return NeighboringList;
    }

    public void setNeighboringList(List<NeighboringCellInfo> neighboringList) {
        NeighboringList = neighboringList;
    }


    private class MyPhoneStateListener extends PhoneStateListener {

    }
}
