package com.example.myapplication3;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {


    private ImageView thumbnailIv;
    private TextView contactTv;
    private FloatingActionButton addFab;

    private static final int CONTACT_PERMISSION_CODE = 1;
    private static final int CONTACT_PICK_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //definir informacoes do contato

        thumbnailIv = findViewById(R.id.thumbnailIv);
        contactTv = findViewById(R.id.contactTv);
        addFab = findViewById(R.id.addFab);

        //listener do botao
        addFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //checar a permissao para ler os contatos
                if (checkContactPermission()){
                    //permissao dada, buscar contatos
                    pickContactIntent();
                }
                else {
                    //permissao negada, peça novamente
                    requestContactPermission();
                }
            }
        });
    }

    private boolean checkContactPermission(){
        //verifica se as permissoes foram habilitadas
        boolean result = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS) == (PackageManager.PERMISSION_GRANTED
        );

        return result;  //verdade se as permissoes foram permitidas, false se nao
    }

    private void requestContactPermission(){
        //pedir permissao
        String[] permission = {Manifest.permission.READ_CONTACTS};

        ActivityCompat.requestPermissions(this, permission, CONTACT_PERMISSION_CODE);
    }

    private void pickContactIntent(){
        //busca o contato
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, CONTACT_PICK_CODE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //resultado da permissao
        if (requestCode == CONTACT_PERMISSION_CODE){
            if (grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //permissao dada
                pickContactIntent();
            }
            else {
                //permissao negada
                Toast.makeText(this, "Permissão negada...", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //cuida do resultado
        if (resultCode == RESULT_OK){
            //quando o usuario clica em um contato

            if (requestCode == CONTACT_PICK_CODE){
                contactTv.setText("");

                Cursor cursor1, cursor2;

                //pega dados
                Uri uri = data.getData();

                cursor1 = getContentResolver().query(uri, null, null, null, null);

                if (cursor1.moveToFirst()){
                    //detalhes do contato
                    String contactId = cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts._ID));
                    String contactName = cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    String contactThumnail = cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
                    String idResults = cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                    int idResultHold = Integer.parseInt(idResults);

                    contactTv.append("ID: "+contactId);
                    contactTv.append("\nName: "+contactName);

                    if (idResultHold == 1){
                        cursor2 = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+contactId,
                                null,
                                null
                        );
                        //numeros do contato
                        while (cursor2.moveToNext()){
                            //pega o numero de telefone
                            String contactNumber = cursor2.getString(cursor2.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            //set details

                            contactTv.append("\nPhone: "+contactNumber);
                            //verifique se tem imagem
                            if (contactThumnail != null){
                                thumbnailIv.setImageURI(Uri.parse(contactThumnail));
                            }
                            else {
                                thumbnailIv.setImageResource(R.drawable.ic_person);
                            }
                        }
                        cursor2.close();
                    }
                    cursor1.close();
                }
            }

        }
        else {
            //voltar
        }
    }
}