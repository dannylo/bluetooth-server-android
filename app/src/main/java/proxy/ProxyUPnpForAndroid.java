package proxy;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.support.annotation.NonNull;

import com.example.dannylo.easy_app.DevicesDiscoveredBluetooth;

import org.eclipse.californium.core.CoapServer;
import org.teleal.cling.UpnpService;
import org.teleal.cling.UpnpServiceImpl;
import org.teleal.cling.android.AndroidUpnpServiceConfiguration;
import org.teleal.cling.binding.annotations.UpnpServiceType;
import org.teleal.cling.binding.xml.Descriptor;
import org.teleal.cling.controlpoint.ActionCallback;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.header.STAllHeader;
import org.teleal.cling.model.meta.Action;
import org.teleal.cling.model.meta.ActionArgument;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.meta.RemoteService;
import org.teleal.cling.model.types.ServiceType;
import org.teleal.cling.model.types.UDAServiceType;
import org.teleal.cling.registry.DefaultRegistryListener;
import org.teleal.cling.registry.Registry;
import org.teleal.cling.registry.RegistryListener;
import org.ufrn.framework.annotation.ProxyTranslate;
import org.ufrn.framework.coapserver.SampleCoapServer;
import org.ufrn.framework.database.access.DeviceManager;
import org.ufrn.framework.proxy.interfaces.IProxy;
import org.ufrn.framework.resources.DefaultCoapInputResource;
import org.ufrn.framework.resources.DefaultCoapOutputResource;
import org.ufrn.framework.util.ManagerFile;
import org.ufrn.framework.virtualentity.VirtualDevice;
import org.teleal.cling.model.meta.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import br.ufrn.framework.virtualentity.resources.Resource;

@ProxyTranslate(description = "ProxyUPnpAndroid")
public class ProxyUPnpForAndroid implements IProxy {

    private UpnpService upnpService;
    private Map<String, Service> mapServices = new HashMap<>();
    private List<String> actionsRepport = new ArrayList<>();


    public static final String FILE_ACTIONS = "actions_discovered.txt";


    public static final String ACTION_KEY = "ACTION_KEY";
    public static final String SERVICE_KEY = "SERVICE_KEY";

    public static final String GetTemperature = "GetTemperature";

    public ProxyUPnpForAndroid(){
        this.upnpService = new UpnpServiceImpl(
                new AndroidUpnpServiceConfiguration(DevicesDiscoveredBluetooth.getWifiManager()));
    }

    private void createListenerUpnp() {
        RegistryListener listenerNetwork = new DefaultRegistryListener() {

            @Override
            public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
                VirtualDevice entity = VirtualDevice.createInstance();
                entity.getIdentification().setDescriptionName(device.getDisplayString());
                entity.getIdentification().setIdProtocol(device.getType().getDisplayString());
                entity.setServer(new CoapServer());

                for (RemoteService serviceRemote : device.getServices()) {
                    Resource resource = new Resource();
                    resource.setDescription(serviceRemote.getServiceId().getId());
                    mapServices.put(serviceRemote.getServiceId().getId(), serviceRemote);
                    for (Action action : serviceRemote.getActions()) {
                        if (!action.hasInputArguments()) {
                            StringBuilder actionName = new StringBuilder(action.getName());
                            actionName.append(":");
                            for(ActionArgument argument: action.getOutputArguments()){
                                actionName.append(argument + ",");
                            }
                            resource.getAction().add(actionName.toString());

                            DefaultCoapOutputResource coap = new DefaultCoapOutputResource(action.getName(),
                                    ProxyUPnpForAndroid.this, entity, serviceRemote.getServiceId().getId(), action.getName());
                            entity.getMappingResources().put(action.getName(), coap);
                            SampleCoapServer.getInstance().add(coap);
                        } else if(action.hasInputArguments()){
                            StringBuilder actionName = new StringBuilder(action.getName());
                            actionName.append("(");
                            for(ActionArgument argument: action.getInputArguments()){
                                actionName.append(argument + ",");
                            }
                            actionName.append(")");
                            resource.getAction().add(actionName.toString());

                            DefaultCoapInputResource coap = new DefaultCoapInputResource(action.getName(),
                                    ProxyUPnpForAndroid.this,
                                    entity,
                                    serviceRemote.getServiceId().getId(),
                                    action.getName());
                            entity.getMappingResources().put(action.getName(), coap);
                            SampleCoapServer.getInstance().add(coap);
                        }
                    }
                    entity.getResources().add(resource);
                }
                DeviceManager.register(entity);
                try {
                    File file = new File(Environment
                            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "/"+ FILE_ACTIONS);
                    if(!file.mkdirs()){
                        System.err.println("Directory actions not created.");
                    }
                    ManagerFile.createFileActionsDiscovery(actionsRepport, file, ManagerFile.ANDROID_TYPE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        upnpService.getRegistry().addListener(listenerNetwork);
        upnpService.getControlPoint().search(new STAllHeader());
    }


    @Override
    public void discoveryAll() {
        System.out.println("Chamando o discovery do UPnP.");
        this.createListenerUpnp();
        try {
            Thread.sleep(5000);
            SampleCoapServer.getInstance().start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean send(VirtualDevice virtualDevice, Map<String, String> map, Map<String, String> map1) throws InterruptedException {
        return false;
    }


    @Override
    public Map<String, String> getData(VirtualDevice virtualDevice, Map<String, String> map) {
        Service serviceUse = mapServices.get(map.get(ProxyUPnpForAndroid.SERVICE_KEY));
        Action action = serviceUse.getAction(map.get(ProxyUPnpForAndroid.ACTION_KEY));
        ActionInvocation actionInvocation = new ActionInvocation(action);

        new ActionCallback.Default(actionInvocation, upnpService.getControlPoint()).run();
        HashMap<String, String> results = new HashMap<>();
        for (ActionArgument actionArgument : action.getOutputArguments()) {
            results.put(actionArgument.getName(),
                    String.valueOf(actionInvocation.getOutput(actionArgument).getValue()));
        }

        return results;
    }
}
