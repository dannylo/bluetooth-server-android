package proxy;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import com.example.dannylo.easy_app.ToolsUtil;
import com.example.dannylo.easy_app.ManageConnectionClient;
import org.eclipse.californium.core.CoapServer;
import org.ufrn.framework.annotation.ProxyTranslate;
import org.ufrn.framework.coapserver.SampleCoapServer;
import org.ufrn.framework.database.access.DeviceManager;
import org.ufrn.framework.proxy.interfaces.IProxy;
import org.ufrn.framework.resources.DefaultCoapInputResource;
import org.ufrn.framework.util.ManagerFile;
import org.ufrn.framework.virtualentity.VirtualDevice;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import br.ufrn.framework.virtualentity.resources.Resource;

@ProxyTranslate(description = "BluetoothProxy")
public class ProxyBluetooth implements IProxy {

    private BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    public static final String VALUE_TERM = "newValue";
    public static final String ACTION = "NewValueTemp";
    public static final String SERVICE = "ConfigureTermostato";
    public static final String FILE_ACTIONS = "actions_discovered.txt";
    public static final String DEVICE_NAME = "XT1068";
    private List<BluetoothDevice> devicesBluetoothDiscovered = new ArrayList<>();

    private static final UUID SENSOR_UUID = UUID.nameUUIDFromBytes("BLUETOOTH SENSOR".getBytes());

    @Override
    public void discoveryAll() {

        Context context = ToolsUtil.getContext().getApplicationContext();

        BroadcastReceiver recieverFound = getReceiver();

        BluetoothDevice deviceFound = null;

        context.registerReceiver(recieverFound, getIntentFound());

        boolean find = false;
        Set<BluetoothDevice> devices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        for(BluetoothDevice device: devices){
            if(device.getName().equalsIgnoreCase(DEVICE_NAME)){
                devicesBluetoothDiscovered.add(device);
                System.out.println("DEVICE FOUND - " + device.getName());
                find = true;
                break;
            }
        }

        if(!find){
            BluetoothAdapter.getDefaultAdapter().startDiscovery();
        }

        if(!devicesBluetoothDiscovered.isEmpty()){
            deviceFound = devicesBluetoothDiscovered.get(0);
        }
        while (deviceFound == null){
            try {
                Thread.sleep(5000);
                deviceFound = devicesBluetoothDiscovered.get(0);
                System.out.println("DEVICE FOUND - " + deviceFound.getName());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        VirtualDevice virtualDevice = VirtualDevice.createInstance();
        virtualDevice.getIdentification().setDescriptionName(deviceFound.getName());
        virtualDevice.getIdentification().setNetworkInfo(deviceFound.getAddress());
        virtualDevice.setServer(new CoapServer());
        virtualDevice.setProxy(this);

        Resource resource = new Resource();
        resource.setDescription(SERVICE);
        resource.getAction().add(ACTION + "(" + VALUE_TERM + ")");
        virtualDevice.getResources().add(resource);

        List<String> actionsRegistered = new ArrayList<>();
        actionsRegistered.add(ACTION + "(" + VALUE_TERM + ")");
        DefaultCoapInputResource defaultCoapInputResource = new DefaultCoapInputResource(ACTION,
                this, virtualDevice, SERVICE,
                ACTION);
        virtualDevice.getMappingResources().put(ACTION, defaultCoapInputResource);
        SampleCoapServer.getInstance().add(defaultCoapInputResource);
        SampleCoapServer.getInstance().start();
        DeviceManager.register(virtualDevice);
        try {
            File file = new File(Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "/"+ FILE_ACTIONS);
            if(!file.mkdirs()){
                System.err.println("Directory actions not created.");
            }
            ManagerFile.createFileActionsDiscovery(actionsRegistered, file, ManagerFile.ANDROID_TYPE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean send(VirtualDevice virtualDevice, Map<String, String> map, Map<String, String> map1) throws InterruptedException {
        ManageConnectionClient manageConnectionClient = new ManageConnectionClient(
                devicesBluetoothDiscovered.get(0),
                adapter,
                map1.get(ProxyBluetooth.VALUE_TERM));
        manageConnectionClient.run();

        return true;
    }

    private static IntentFilter getIntentFound(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        return filter;
    }

    private BroadcastReceiver getReceiver(){
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(action.equals(BluetoothDevice.ACTION_FOUND)){
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if(device.getName() != null && device.getName().equalsIgnoreCase(ProxyBluetooth.DEVICE_NAME)){
                        devicesBluetoothDiscovered.add(device);
                    }
                }
            }
        };
    }

    @Override
    @Deprecated
    public Map<String, String> getData(VirtualDevice virtualDevice, Map<String, String> map) {
        return null;
    }


}
