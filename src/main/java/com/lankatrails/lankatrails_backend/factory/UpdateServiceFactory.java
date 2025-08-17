package com.lankatrails.lankatrails_backend.factory;

import com.lankatrails.lankatrails_backend.dtos.response.TransportResponseDTO;
import com.lankatrails.lankatrails_backend.model.Transport;
import org.springframework.stereotype.Component;

@Component
public class UpdateServiceFactory {

    public Transport updateTransport(Transport transport,Transport request){
//        transport.setVehicleType(request.getVehicleType());
        transport.setVehicleQty(request.getVehicleQty());
        return transport;
    }


}
