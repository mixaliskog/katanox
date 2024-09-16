package com.katanox.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class BookingService {


    @Autowired
    RabbitMQBookingSenderService rabbitMQBookingSenderService;


    public int booking(BookingRequest bookingRequest){
        // persist booking and get id
        var bookingId= new Random().nextInt();
        //Map to BookingDTO
        var bookingDTO = new BookingDTO();
        rabbitMQBookingSenderService.ObjectRabbitMQSender(bookingDTO);
        return bookingId;

    }
}
