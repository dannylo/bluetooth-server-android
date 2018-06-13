package proxy;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Environment;

import com.example.dannylo.easy_app.ComunicationDevice;
import com.example.dannylo.easy_app.DevicesDiscoveredBluetooth;
import com.example.dannylo.easy_app.ManageConnectionClient;

import org.eclipse.californium.core.CoapServer;
import org.ufrn.framework.annotation.ProxyTranslate;
import org.ufrn.framework.coapserver.SampleCoapServer;
import org.ufrn.framework.database.access.DeviceManager;
import org.ufrn.framework.proxy.implementations.UPnpProxy;
import org.ufrn.framework.proxy.interfaces.IProxy;
import org.ufrn.framework.resources.DefaultCoapInputResource;
import org.ufrn.framework.util.ManagerFile;
import org.ufrn.framework.virtualentity.VirtualDevice;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import br.ufrn.framework.virtualentity.resources.Resource;
import javassist.tools.rmi.Sample;

@ProxyTranslate(description = "BluetoothProxy")
public class ProxyBluetooth implements IProxy {

    private ComunicationDevice comunicationDevice;
    private BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

    public static final String VALUE_TERM = "newValue";

    public static final String ACTION = "NewValueTemp(newValue)";
    public static final String FILE_ACTIONS = "actions_discovered.txt";


    @Override
    public void discoveryAll() {
        BluetoothDevice device = null;

        if(!DevicesDiscoveredBluetooth.getList().isEmpty()){
           device = DevicesDiscoveredBluetooth.getList().get(0);
        }
        //Enquanto o dispositivo Bluetooth não for descoberto e pareado, o loop rodará e dormirá por 3 seg.
        while (device == null){
            try {
                Thread.sleep(5000);
                device = DevicesDiscoveredBluetooth.getList().get(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        VirtualDevice virtualDevice = VirtualDevice.createInstance();
        virtualDevice.getIdentification().setDescriptionName(device.getName());
        virtualDevice.getIdentification().setNetworkInfo(device.getAddress());
        virtualDevice.setServer(new CoapServer());
        virtualDevice.setProxy(this);

        Resource resource = new Resource();
        resource.setDescription("ConfigureTermostato");
        resource.getAction().add("NewValueTemp(newValue)");
        virtualDevice.getResources().add(resource);

        List<String> actionsRegistered = new ArrayList<>();
        actionsRegistered.add("NewValueTemp(newValue)");

        DefaultCoapInputResource defaultCoapInputResource = new DefaultCoapInputResource("NewValueTemp(newValue)",
                this, virtualDevice, "ConfigureTermostato",
                "NewValueTemp(newValue)");

        virtualDevice.getMappingResources().put("NewValueTemp(newValue)", defaultCoapInputResource);
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
        System.out.println("Send is invoked by proxy: " + map.get(UPnpProxy.SERVICE_KEY) + ": " + map.get(UPnpProxy.ACTION_KEY));
        ManageConnectionClient manageConnectionClient = new ManageConnectionClient(
                DevicesDiscoveredBluetooth.getList().get(0),
                adapter,
                map1.get(ProxyBluetooth.VALUE_TERM));
        manageConnectionClient.run();

        return true;
    }

    @Override
    @Deprecated
    public Map<String, String> getData(VirtualDevice virtualDevice, Map<String, String> map) {
        return null;
    }


}
