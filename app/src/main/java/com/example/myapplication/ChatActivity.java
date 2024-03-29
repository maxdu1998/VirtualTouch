package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.graphics.Color;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.Item;
import com.xwray.groupie.OnItemClickListener;
import com.xwray.groupie.ViewHolder;

import org.apache.commons.text.StringEscapeUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {

    private GroupAdapter adapter;
    private static User user;
    private static User me;
    // String Rock = "\\uD83D\\uDC4D";
    // String Joinha = "\\uD83E\\uDD18";

    String Like = "\\uD83D\\uDC4D";
    String ToPoint = "\\u261D\\uFE0F";
    String Fuck = "\\uD83D\\uDD95";
    String Rock = "\\uD83E\\uDD18";
    String Ok = "\\uD83D\\uDC4C";
    String HangLoose = "\\uD83E\\uDD19";
    String Peace = "\\u270C\\uFE0F";
    String Close = "\\u270A";
    String Open = "\\u270B";

    private MediaPlayer mediaPlayer;
    boolean mStartRecording = true;
    boolean mStartPlay = true;
    private MediaRecorder recorder = null;
    private MediaPlayer   player = null;
    String baseFileName = null;
    String fileName = null;
    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private MessageItem lastAudio = null;

    private static Menu menu;

    static ConnectionThread connect;
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
        final Button btnChat = findViewById(R.id.btn_chat);
        final Button btnGravar = findViewById(R.id.btn_Audio);
        isConnected = false;

        baseFileName = getExternalCacheDir().getAbsolutePath();
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        btnGravar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    btnGravar.setBackground(getDrawable(R.drawable.ic_twotone_mic_record));
                    mStartRecording = true;
                    onRecord(mStartRecording);
                }

                else  if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    btnGravar.setBackground(getDrawable(R.drawable.ic_twotone_mic));
                    mStartRecording = false;
                    onRecord(mStartRecording);
                }
                return false;
            }
        });
        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendMessage();
                //sendUrlToMediaPlayer("https://firebasestorage.googleapis.com/v0/b/virtualtouch-3c24c.appspot.com/o/audio%2F5e39b823-bd9d-49e1-af61-e4e3cbcd2ff7?alt=media&token=d9d3bcb9-1fda-40d3-a475-bbb1094c283e");
                //sendUrlToMediaPlayer(fileName);
            }
        });

        btnChat.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                fetchBluetooth();
                return false;
            }
        });


        editChat.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String txt = editChat.getText().toString().replaceAll(" ", "");
                if(txt.length()>0) {
                    btnChat.setVisibility(View.VISIBLE);
                    btnGravar.setVisibility(View.INVISIBLE);
                }
                else{
                    btnChat.setVisibility(View.INVISIBLE);
                    btnGravar.setVisibility(View.VISIBLE);
                }
            }
        });

        adapter = new GroupAdapter();
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull Item item, @NonNull View view) {
               // Intent intent = new Intent(ChatActivity.this, ChatActivity.class);
                MessageItem msg = (ChatActivity.MessageItem) item;
                if(msg.message.getIsAudio()){
                    if(lastAudio != msg && lastAudio != null && !mStartPlay){
                        onPlay(false, lastAudio.message.getText(), lastAudio.imgAudio);
                        Log.d("Audio", "Dois audio parar 1");

                    }
                    lastAudio = msg;
                    onPlay(mStartPlay, msg.message.getText(), msg.imgAudio);
                    mStartPlay =! mStartPlay;
                    Log.d("Audio", "Tocar");
                }

            }
        });

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


    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void onPlay(boolean start, String url, ImageView img) {
        if (start) {
            startPlaying(url, img);
        } else {
            stopPlaying(img);
        }
    }
    /*private void startPlaying() {
        player = new MediaPlayer();
        try {
            player.setDataSource(fileName);
            player.prepare();
            player.start();
        } catch (IOException e) {
            Log.e("Audio", "prepare() failed");
        }
    }
*/
    void startPlaying(String url, final ImageView img) {
        try {
            player = new MediaPlayer();
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setDataSource(url);

            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    player.start();
                    img.setBackground(getDrawable(R.drawable.ic_round_headset_on));
                }
            });
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopPlaying(img);
                }
            });

            player.prepareAsync();
        } catch (IOException err) {
            Log.e("Audio", err.toString());
        }
    }

    private void stopPlaying(ImageView img) {
        img.setBackground(getDrawable(R.drawable.ic_round_headset_off));
        player.stop();
        player.reset();
        player.release();
        mStartPlay = true;
        player = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();

    }

    private void startRecording() {

        fileName = baseFileName+"/record.3gp";
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e("Audio", "prepare() failed");
        }

        recorder.start();
    }

    private void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;
        Log.d("Audio", "Gravado!");
        Uri file = Uri.fromFile(new File(fileName));
        final StorageReference ref = FirebaseStorage.getInstance().getReference("/audio/" + UUID.randomUUID().toString());
        ref.putFile(file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        final String MeuId = FirebaseAuth.getInstance().getUid();
                        final String contatoId = user.getUuid();
                        long timestamp = System.currentTimeMillis();

                        final Message message = new Message();
                        message.setFromId(MeuId);
                        message.setToId(contatoId);
                        message.setTimestamp(timestamp);
                        message.setText(uri.toString());
                        message.setIsAudio(true);

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
                                            contact.setIsAudio(message.getIsAudio());

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
                                            contact.setIsAudio(message.getIsAudio());

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
                });


            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Teste", e.getMessage(), e);
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
                                                        || t5.contains(ToPoint)
                                                        || t5.contains(Fuck)
                                                        || t5.contains(Rock)
                                                        || t5.contains(Ok)
                                                        || t5.contains(HangLoose)
                                                        || t5.contains(Peace)
                                                        || t5.contains(Close)
                                                        || t5.contains(Open)
                                        )) {
                                            if (isConnected && connect.isConnected) {
                                                Log.d("BT_R", message.getText());

                                                if (t5.contains(Like))
                                                    connect.write(("0\n").getBytes());
                                                if (t5.contains(ToPoint))
                                                    connect.write(("1\n").getBytes());
                                                if (t5.contains(Fuck))
                                                    connect.write(("2\n").getBytes());
                                                if (t5.contains(Rock))
                                                    connect.write(("3\n").getBytes());
                                                if (t5.contains(Ok))
                                                    connect.write(("4\n").getBytes());
                                                if (t5.contains(HangLoose))
                                                    connect.write(("5\n").getBytes());
                                                if (t5.contains(Peace))
                                                    connect.write(("6\n").getBytes());
                                                if (t5.contains(Close))
                                                    connect.write(("7\n").getBytes());
                                                if (t5.contains(Open))
                                                    connect.write(("8\n").getBytes());

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
/*
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
            */

        }
    }

    private static void fetchMessagesBluetooth(){
        if (me != null) {
            final String MeuId = me.getUuid();
            final String contatoId = user.getUuid();
            final boolean delete = true;
            FirebaseFirestore.getInstance().collection("/handComand")
                    .document(MeuId)
                    .collection(contatoId)
                    .orderBy("timestamp", Query.Direction.ASCENDING)
                    .addSnapshotListener(activity, new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            List<DocumentChange> documentChanges = queryDocumentSnapshots.getDocumentChanges();

                            if (documentChanges != null) {
                                for (DocumentChange doc : documentChanges) {
                                    if (doc.getType() == DocumentChange.Type.ADDED) {
                                        Message message = doc.getDocument().toObject(Message.class);
                                        Log.d("BT_R", message.getText());
                                        connect.write((message.getText()+'\n').getBytes());
                                        //connect.write(("Teste").getBytes());
                                        if(delete)
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
            //Base que guarda as minhas msg enviadas
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
        public ImageView imgAudio;
        private MessageItem(Message message) {
            this.message = message;
        }

        @Override
        public void bind(@NonNull ViewHolder viewHolder, int position) {
            TextView txtMsg = viewHolder.itemView.findViewById(R.id.txt_msg);
            ImageView imgMessage = viewHolder.itemView.findViewById(R.id.img_message_user);
            if(message.getIsAudio()){
                imgAudio = viewHolder.itemView.findViewById(R.id.img_audio);
                imgAudio.setBackground(getDrawable(R.drawable.ic_round_headset_off));
                txtMsg.setText("Audio");
            }

            else
                txtMsg.setText(message.getText());

            Picasso.get()
                    .load(message.getFromId().equals(FirebaseAuth.getInstance().getUid())
                            ? me.getProfileUrl()
                            : user.getProfileUrl())
                    .into(imgMessage);

        }

        @Override
        public int getLayout() {

            if(message.getIsAudio()){
                return message.getFromId().equals(FirebaseAuth.getInstance().getUid())
                        ? R.layout.item_from_audio
                        : R.layout.item_to_audio;
            }
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

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        this.menu = menu;
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btn_bletooth:
                if(!isConnected){
                    fetchBluetooth();
                }
                else{
                    if (connect != null)
                        connect.cancel();
                    menu.getItem(0).setTitle(R.string.bluetoothOFF);
                }

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void connectBT(String mac) {


        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            return;
        }

        btAdapter.enable();

        connect = new ConnectionThread(mac);
        connect.start();

        try {
            Thread.sleep(1000);
        } catch (Exception E) {
            E.printStackTrace();
        }
        isConnected = connect.isConnected;

        //Muda Cor ao Conectar BT
        /*
        if (isConnected = true) {
            findViewById(R.id.btn_chat).setBackgroundResource(R.drawable.bg_button_rounded_green);
            getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            findViewById(R.id.btn_chat).setBackgroundResource(R.drawable.bg_button_rounded);
            getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
         */

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

                menu.getItem(0).setTitle(R.string.bluetoothOFF);
                if(isConnected)
                    Toast.makeText(activity, "Disconnected", Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(activity, "Connection ERROR", Toast.LENGTH_LONG).show();
                isConnected = false;
            }
            //statusMessage.setText("Ocorreu um erro durante a conexão D:");


            else if (dataString.equals("---S")) {
                isConnected = true;
                menu.getItem(0).setTitle(R.string.bluetoothON);
                Toast.makeText(activity, "Connection SUCCESS", Toast.LENGTH_LONG).show();
                fetchMessagesBluetooth();
            } else {
                sendMessageBT(dataString);
            }

        }
    };
    /*--------------------------------------------teste---------------------------------------*/
}
