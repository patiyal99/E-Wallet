package com.example.jbdl.major_project;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.swing.*;

import static com.example.jbdl.major_project.CommonConstants.*;
import static  com.example.jbdl.major_project.TransactionStatus.PENDING;

import java.util.UUID;

@Service
public class TransactionService {
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    KafkaTemplate<String,String> kafkaTemplate;

    @Autowired
    TransactionRepository transactionRepository;

    public String doTransaction(TransactionRequest transactionRequest) throws JsonProcessingException {

        Transaction transaction = Transaction.builder()
                .transactionId(UUID.randomUUID().toString())
                .transactionStatus(PENDING)
                .amount(transactionRequest.getAmount())
                .purpose(transactionRequest.getPurpose())
                .sender(transactionRequest.getSender())
                .receiver(transactionRequest.getReceiver())
                .build();

        transactionRepository.save(transaction);

        JSONObject transactionCreateRequest = new JSONObject();
        transactionCreateRequest.put(AMOUNT_ATTRIBUTE,transaction.getAmount());
        transactionCreateRequest.put(SENDER_ATTRIBUTE,transaction.getSender());
        transactionCreateRequest.put(RECEIVER_ATTRIBUTE,transaction.getReceiver());
        transactionCreateRequest.put(TRANSACTION_ID_ATTRIBUTE,transaction.getTransactionId());

        kafkaTemplate.send(TRANSACTION_CREATE_KAFKA_TOPIC,objectMapper.writeValueAsString(transactionCreateRequest));

        return transaction.getTransactionId();
    }

    @KafkaListener(topics = {WALLET_UPDATE_KAFKA_TOPIC},groupId = "jbdl123_grp")
    public void updateTransaction(String msg) throws JsonProcessingException {
        JSONObject walletUpdate = objectMapper.readValue(msg,JSONObject.class);

        String transactionId = (String) walletUpdate.get(TRANSACTION_ID_ATTRIBUTE);
        String walletUpdateStatus = (String) walletUpdate.get(WALLET_UPDATE_STATUS_ATTRIBUTE);
        String receiver = (String) walletUpdate.get(RECEIVER_ATTRIBUTE);
        String sender = (String) walletUpdate.get(SENDER_ATTRIBUTE);
        Double amount = (Double) walletUpdate.get(AMOUNT_ATTRIBUTE);

        TransactionStatus transactionStatus=TransactionStatus.FAILED;

        if(WALLET_UPDATE_SUCCESS_STATUS.equals(walletUpdateStatus)){
            transactionStatus=TransactionStatus.SUCCESS;
            transactionRepository.updateTxn(transactionId,TransactionStatus.SUCCESS);
        }else{
            transactionRepository.updateTxn(transactionId,TransactionStatus.FAILED);
        }

        //failed - sender
        //success - sender,receiver
        String senderSuccessMsg="Transaction with ID "+ transactionId + " has been completed .Your account has been debited by amount"+ amount ;
        String senderFailedMsg="Transaction with ID "+ transactionId + " got failed. Please try again.";
        String receiverMsg="You have received payment of " + amount + " from " + sender;

        String senderMsg = WALLET_UPDATE_SUCCESS_STATUS.equals(walletUpdateStatus) ? senderSuccessMsg: senderFailedMsg;


        JSONObject jsonObject=new JSONObject();
        jsonObject.put(EMAIL_ATTRIBUTE,sender);
        jsonObject.put("isSender",true);
        jsonObject.put(AMOUNT_ATTRIBUTE,amount);
        jsonObject.put(TRANSACTION_ID_ATTRIBUTE,transactionId);
        jsonObject.put(TRANSACTION_STATUS_ATTRIBUTE,transactionStatus.name());

        kafkaTemplate.send(TRANSACTION_COMPLETE_KAFKA_TOPIC,objectMapper.writeValueAsString(jsonObject));

        if(WALLET_UPDATE_SUCCESS_STATUS.equals(walletUpdateStatus)){
            jsonObject=new JSONObject();
            jsonObject.put(EMAIL_ATTRIBUTE,receiver);
            jsonObject.put("isSender",false);
            jsonObject.put(AMOUNT_ATTRIBUTE,amount);
            jsonObject.put(TRANSACTION_ID_ATTRIBUTE,transactionId);
            jsonObject.put(TRANSACTION_STATUS_ATTRIBUTE,transactionStatus.name());

            kafkaTemplate.send(TRANSACTION_COMPLETE_KAFKA_TOPIC,objectMapper.writeValueAsString(jsonObject));
        }
    }
}
