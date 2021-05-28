package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.Item;
import com.xwray.groupie.ViewHolder;

import org.apache.commons.text.StringEscapeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ChatActivity extends AppCompatActivity {

    private GroupAdapter adapter;
    private static User user;
    private  User me;
    // String Rock = "\\uD83D\\uDC4D";
    // String Joinha = "\\uD83E\\uDD18";

    String Like         = "\\uD83D\\uDC4D";
    String ToPoint      = "\\u261D\\uFE0F";
    String Fuck         = "\\uD83D\\uDD95";
    String Rock         = "\\uD83E\\uDD18";
    String Ok           = "\\uD83D\\uDC4C";
    String HangLoose    = "\\uD83E\\uDD19";
    String Peace        = "\\u270C\\uFE0F";
    String Close        = "\\u270A";
    String Open         = "\\u270B";

    ConnectionThread connect;
    //public boolean isConnected = false;
    static Activity activity;

    private EditText editChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_chat);

        user = getIntent().getExtras().getParcelable("user");
        getSupportActionBar().setTitle(user.getUsername());
        activity = ChatActivity.this;
        RecyclerView rv = findViewById(R.id.recycler_chat);
        editChat = findViewById(R.id.edit_chat);
        Button btnChat = findViewById(R.id.btn_chat);
        isConnected = false;
        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        btnChat.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                fetchBluetooth();
                return false;
            }
        });


        adapter = new GroupAdapter();
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        FirebaseFirestore.getInstance().collection("/users")
                .document(FirebaseAuth.getInstance().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        me = documentSnapshot.toObject(User.class);
                        fetchMessages();
                    }
                });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (connect != null)
            connect.cancel();
        Log.d("Error", "Destruido");
    }

    private void fetchMessages() {
        if (me != null) {

            final String MeuId = me.getUuid();
            final String contatoId = user.getUuid();
            FirebaseFirestore.getInstance().collection("/conversations")
                    .document(MeuId)
                    .collection(contatoId)
                    .orderBy("timestamp", Query.Direction.ASCENDING)
                    .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            List<DocumentChange> documentChanges = queryDocumentSnapshots.getDocumentChanges();

                            if (documentChanges != null) {
                                for (DocumentChange doc : documentChanges) {
                                    if (doc.getType() == DocumentChange.Type.ADDED) {
                                        Message message = doc.getDocument().toObject(Message.class);
                                        String t5 = StringEscapeUtils.escapeJava(message.getText());
                                        if (!message.getFromId().equals(FirebaseAuth.getInstance().getUid()) && (
                                                t5.contains(Like)
                                                        || t5.contains(ToPoint  )
                                                        || t5.contains(Fuck     )
                                                        || t5.contains(Rock     )
                                                        || t5.contains(Ok       )
                                                        || t5.contains(HangLoose)
                                                        || t5.contains(Peace    )
                                                        || t5.contains(Close    )
                                                        || t5.contains(Open     )
                                        )) {
                                            if (isConnected && connect.isConnected){

                                                if(t5.contains(Like))
                                                    connect.write(("0").getBytes());
                                                if(t5.contains(ToPoint))
                                                    connect.write(("1").getBytes());
                                                if(t5.contains(Fuck))
                                                    connect.write(("2").getBytes());
                                                if(t5.contains(Rock))
                                                    connect.write(("3").getBytes());
                                                if(t5.contains(Ok))
                                                    connect.write(("4").getBytes());
                                                if(t5.contains(HangLoose))
                                                    connect.write(("5").getBytes());
                                                if(t5.contains(Peace))
                                                    connect.write(("6").getBytes());
                                                if(t5.contains(Close))
                                                    connect.write(("7").getBytes());
                                                if(t5.contains(Open))
                                                    connect.write(("8").getBytes());

                                                /*
                                                t5 = t5.replaceAll(Like     , "0:");
                                                t5 = t5.replaceAll(ToPoint  , "1:");
                                                t5 = t5.replaceAll(Fuck     , "2:");
                                                t5 = t5.replaceAll(Rock     , "3:");
                                                t5 = t5.replaceAll(Ok       , "4:");
                                                t5 = t5.replaceAll(HangLoose, "5:");
                                                t5 = t5.replaceAll(Peace    , "6:");
                                                t5 = t5.replaceAll(Close    , "7:");
                                                t5 = t5.replaceAll(Open     , "8:");

                                                String[] t6 = t5.split(":");
                                                for( String t7 : t6) {
                                                    connect.write((t7 + "\n").getBytes());
                                                }

                                                 */

                                            }

                                        }
                                        //else
                                        adapter.add(new MessageItem(message));
                                        RecyclerView rv = findViewById(R.id.recycler_chat);
                                        rv.smoothScrollToPosition(adapter.getItemCount() - 1);
                                    }
                                }
                            }
                        }
                    });

            FirebaseFirestore.getInstance().collection("/handComand")
                    .document(MeuId)
                    .collection(contatoId)
                    .orderBy("timestamp", Query.Direction.ASCENDING)
                    .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            List<DocumentChange> documentChanges = queryDocumentSnapshots.getDocumentChanges();

                            if (documentChanges != null) {
                                for (DocumentChange doc : documentChanges) {
                                    if (doc.getType() == DocumentChange.Type.ADDED) {
                                        Message message = doc.getDocument().toObject(Message.class);
                                        Log.d("BT_R", message.getText());
                                        FirebaseFirestore.getInstance().collection("/handComand")
                                                .document(MeuId)
                                                .collection(contatoId)
                                                .document(doc.getDocument().getId())
                                                .delete();
                                    }
                                }
                            }
                        }
                    });

        }
    }

    private void sendMessage() {
        String text = editChat.getText().toString();

        editChat.setText(null);

        final String MeuId = FirebaseAuth.getInstance().getUid();
        final String contatoId = user.getUuid();
        long timestamp = System.currentTimeMillis();

        final Message message = new Message();
        message.setFromId(MeuId);
        message.setToId(contatoId);
        message.setTimestamp(timestamp);
        message.setText(text);

        if (!message.getText().isEmpty()) {
            //Base que garda as minhas msg enviadas
            FirebaseFirestore.getInstance().collection("/conversations")
                    .document(MeuId)
                    .collection(contatoId)
                    .add(message)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d("Teste", documentReference.getId());

                            Contact contact = new Contact();
                            contact.setUuid(contatoId);
                            contact.setUsername(user.getUsername());
                            contact.setPhotoUrl(user.getProfileUrl());
                            contact.setTimestamp(message.getTimestamp());
                            contact.setLastMessage(message.getText());

                            FirebaseFirestore.getInstance().collection("/last-messages")
                                    .document(MeuId)
                                    .collection("contacts")
                                    .document(contatoId)
                                    .set(contact);
/*
                            if (!user.isOnline()) {
                                Notification notification = new Notification();
                                /*notification.setFromId(message.getFromId());
                                notification.setToId(message.getToId());
                                notification.setTimestamp(message.getTimestamp());
                                notification.setText(message.getText());
                                notification.setFromName(me.getUsername());

                                FirebaseFirestore.getInstance().collection("/notifications")
                                        .document(user.getToken())
                                        .set(notification);
                            }

                             */
                        }

                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("Teste", e.getMessage(), e);
                        }
                    });

            //Envia para o contato a mensagem
            FirebaseFirestore.getInstance().collection("/conversations")
                    .document(contatoId)
                    .collection(MeuId)
                    .add(message)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d("Teste", documentReference.getId());

                            Contact contact = new Contact();
                            contact.setUuid(contatoId);
                            contact.setUsername(me.getUsername());
                            contact.setPhotoUrl(me.getProfileUrl());
                            contact.setTimestamp(message.getTimestamp());
                            contact.setLastMessage(message.getText());

                            FirebaseFirestore.getInstance().collection("/last-messages")
                                    .document(contatoId)
                                    .collection("contacts")
                                    .document(MeuId)
                                    .set(contact);


                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("Teste", e.getMessage(), e);
                        }
                    });
        }
    }

    private class MessageItem extends Item<ViewHolder> {

        private final Message message;

        private MessageItem(Message message) {
            this.message = message;
        }

        @Override
        public void bind(@NonNull ViewHolder viewHolder, int position) {
            TextView txtMsg = viewHolder.itemView.findViewById(R.id.txt_msg);
            ImageView imgMessage = viewHolder.itemView.findViewById(R.id.img_message_user);

            txtMsg.setText(message.getText());

            Picasso.get()
                    .load(message.getFromId().equals(FirebaseAuth.getInstance().getUid())
                            ? me.getProfileUrl()
                            : user.getProfileUrl())
                    .into(imgMessage);
        }

        @Override
        public int getLayout() {

            return message.getFromId().equals(FirebaseAuth.getInstance().getUid())
                    ? R.layout.item_from_message
                    : R.layout.item_to_message;
        }
    }

    private static void sendMessageBT(String text) {
        //String text = editChat.getText().toString();

        //editChat.setText(null);
        Log.d("BTE", text);
        final String MeuId = FirebaseAuth.getInstance().getUid();
        final String contatoId = user.getUuid();
        long timestamp = System.currentTimeMillis();

        final Message message = new Message();
        message.setFromId(MeuId);
        message.setToId(contatoId);
        message.setTimestamp(timestamp);
        message.setText(text);

        if (!message.getText().isEmpty()) {

            FirebaseFirestore.getInstance().collection("/handComand")
                    .document(contatoId)
                    .collection(MeuId)
                    .add(message)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d("Teste", documentReference.getId());
/*
                            Contact contact = new Contact();
                            contact.setUuid(contatoId);
                            contact.setUsername(me.getUsername());
                            contact.setPhotoUrl(me.getProfileUrl());
                            contact.setTimestamp(message.getTimestamp());
                            contact.setLastMessage(message.getText());

                            FirebaseFirestore.getInstance().collection("/last-messages")
                                    .document(contatoId)
                                    .collection("contacts")
                                    .document(MeuId)
                                    .set(contact);*/


                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("Teste", e.getMessage(), e);
                        }
                    });
        }
    }


    private void connectBT(String mac) {


        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            //statusMessage.setText("Que pena! Hardware Bluetooth não está funcionando :(");
        } else {
            //statusMessage.setText("Ótimo! Hardware Bluetooth está funcionando :)");
        }

        /* A chamada do seguinte método liga o Bluetooth no dispositivo Android
            sem pedido de autorização do usuário. É altamente não recomendado no
            Android Developers, mas, para simplificar este app, que é um demo,
            faremos isso. Na prática, em um app que vai ser usado por outras
            pessoas, não faça isso.
         */
        btAdapter.enable();

        //connect = new ConnectionThread("98:D3:31:F5:2A:C9");
        connect = new ConnectionThread(mac);
        connect.start();

        /* Um descanso rápido, para evitar bugs esquisitos.
         */
        try {
            Thread.sleep(1000);
        } catch (Exception E) {
            E.printStackTrace();
        }
        isConnected = connect.isConnected;
    }

    private static boolean isConnected;

    private void fetchBluetooth() {

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            String[] agenda = new String[pairedDevices.size()];
            int pos = 0;
            for (BluetoothDevice device : pairedDevices) {
                agenda[pos] = device.getName();
                pos++;
            }
            Dialog dia = onCreateDialog(agenda, pairedDevices);
            dia.show();
        }
    }

    private Dialog onCreateDialog(final String[] agenda, final Set<BluetoothDevice> bluetoothDevices) {
        //final String [] items=agenda.toArray(String);
        final AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
        builder.setTitle("Escolha")
                .setItems(agenda, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        String mac = "";
                        for (BluetoothDevice device : bluetoothDevices) {
                            if (device.getName().equals(agenda[which])) {
                                mac = device.getAddress();
                                connectBT(mac);
                                break;
                            }
                        }
                    }
                });
        return builder.create();
    }



    /*--------------------------------------------teste---------------------------------------*/
    public static Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {

            Bundle bundle = msg.getData();
            byte[] data = bundle.getByteArray("data");
            String dataString = new String(data);

            if (dataString.equals("---N")) {
                isConnected = false;
                Toast.makeText(activity, "Connection ERROR", Toast.LENGTH_LONG).show();
            }
            //statusMessage.setText("Ocorreu um erro durante a conexão D:");


            else if (dataString.equals("---S")) {
                isConnected = true;
                Toast.makeText(activity, "Connection SUCCESS", Toast.LENGTH_LONG).show();
                Log.d("Btt", "Conectado :D");
            } else {
                sendMessageBT(dataString);
            }

        }
    };
    /*--------------------------------------------teste---------------------------------------*/
}
