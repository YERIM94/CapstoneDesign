package aka.capstonedesign;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.support.design.widget.Snackbar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.pm.ActivityInfo;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.signature.StringSignature;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.ContentValues.TAG;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseUser mFirebaseUser;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseStorage mFirebaseStorage;
    private TextView etname;
    private Spinner spage;
    private Spinner spvarieties;
    private String Key;
    private String weight;
    private final Integer[] age = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};

    Uri imageuri, photoURI, albumURI;
    Uri storageURI;
    String mCurrentPhotoPath;
    private static final int MY_PERMISSION_CAMERA = 1111;
    private static final int REQUEST_TAKE_PHOTO = 2222;
    private static final int REQUEST_TAKE_ALBUM = 3333;
    private static final int REQUEST_IMAGE_CROP = 4444;
    CircleImageView profile_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //파이어베이스 인증&데이터베이스 참조 생성
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        //프로필 사진
        profile_image = (CircleImageView) findViewById(R.id.profile);

        //나이 선택 spinner
        spage = (Spinner) findViewById(R.id.spinner_age);
        ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_dropdown_item, age);
        spage.setAdapter(adapter);

        //견종 선택 spinner
        spvarieties = (Spinner) findViewById(R.id.spinner_varieties);
        ArrayAdapter adapter1 = ArrayAdapter.createFromResource(this, R.array.dog_varieties, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spvarieties.setAdapter(adapter1);

        //창 닫기
        ImageButton button = (ImageButton) findViewById(R.id.btn_close);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //프로필 사진 변경
        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //카메라or앨범 선택
                DialogInterface.OnClickListener cameraListner = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TakePhoto();
                    }
                };
                DialogInterface.OnClickListener albumListner = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TakeAlbum();
                    }
                };
                new AlertDialog.Builder(ProfileActivity.this)
                        .setTitle("프로필 이미지 선택")
                        .setPositiveButton("사진촬영", cameraListner)
                        .setNegativeButton("앨범선택", albumListner)
                        .show();

                checkPermission();
            }
        });

        //프로필 저장 버튼
        Button savebutton = (Button) findViewById(R.id.btn_save_profile);
        savebutton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfile();
            }
        });

        //이름 입력 Textview
        etname = (TextView) findViewById(R.id.profile_name);
        etname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(ProfileActivity.this);
                alert.setTitle("이름입력");
                final EditText name = new EditText(ProfileActivity.this);
                alert.setView(name);
                alert.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String DogName = name.getText().toString();
                        etname.setText(DogName);
                        dialog.dismiss();
                    }
                });
                alert.show();
            }
        });

        //무게 선택 버튼
        final ImageButton bcs1 = (ImageButton) findViewById(R.id.bcs1);
        ImageButton bcs2 = (ImageButton) findViewById(R.id.bcs2);
        ImageButton bcs3 = (ImageButton) findViewById(R.id.bcs3);

        bcs1.setOnClickListener(new View.OnClickListener() { //마름 사진 선택
            @Override
            public void onClick(View v) {
                weight = "thin";
                Toast.makeText(ProfileActivity.this, "마름", Toast.LENGTH_SHORT).show();
            }
        });
        bcs2.setOnClickListener(new View.OnClickListener() { //보통 사진 선택
            @Override
            public void onClick(View v) {
                weight = "normal";
                Toast.makeText(ProfileActivity.this, "보통", Toast.LENGTH_SHORT).show();
            }
        });
        bcs3.setOnClickListener(new View.OnClickListener() { //과체중 사진 선택
            @Override
            public void onClick(View v) {
                weight = "fat";
                Toast.makeText(ProfileActivity.this, "과체중", Toast.LENGTH_SHORT).show();
            }
        });

        //액티비티 실행할 때 프로필 이미지 설정
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) { //초기에 한번 불러오고, 데이터가 바뀌면 다시 불러옴
                StorageUri storageUri = dataSnapshot.getValue(StorageUri.class);
                String profilename = storageUri.getUri();
                setProfileImage(profile_image,profilename);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };
        mFirebaseDatabase.getReference(mFirebaseUser.getUid()).child("StorageUri").addValueEventListener(postListener);

    }

    //프로필 내용 저장
    private void saveProfile() {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final Profile profile = new Profile();

        profile.setName(etname.getText().toString());
        profile.setAge(spage.getSelectedItem().hashCode());
        profile.setVarieties(spvarieties.getSelectedItem().toString());
        profile.setWeight(weight);

        //Profile에 하위 노드가 없으면 새로 저장
        if (mFirebaseDatabase.getReference(user.getUid()).child("Profile") == null) {
            mFirebaseDatabase.getReference(user.getUid()).child("Profile").setValue(profile).addOnSuccessListener(
                    ProfileActivity.this, new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //profileKey = mFirebaseDatabase.getReference(user.getUid()).push().getKey();
                            Snackbar.make(etname, "프로필이 저장되었습니다.", Snackbar.LENGTH_LONG).show();
                        }
                    });
        } else //Profile 하위 노드가 있으면 원래 내용 수정
        {
            Map<String, Object> profileValues = profile.toMap();
            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put("/Profile", profileValues);
            mFirebaseDatabase.getReference(user.getUid()).updateChildren(childUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Snackbar.make(etname, "프로필이 저장되었습니다.", Snackbar.LENGTH_LONG).show();
                }
            });
        }

    }

    //사진찍어서 프로필로 지정
    public void TakePhoto() {
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(getPackageManager()) != null) {
                File photo = null;
                try {
                    photo = createImageFile();
                } catch (IOException ex) {
                    //Log.e("captureCamera Error");
                }
                if (photo != null) {
                    Uri providerURI = FileProvider.getUriForFile(this, getPackageName(), photo);
                    imageuri = providerURI;

                    intent.putExtra(MediaStore.EXTRA_OUTPUT, providerURI);
                    startActivityForResult(intent, REQUEST_TAKE_PHOTO);
                }
            }
        }
    }

    //앨범에서 사진선택 후 프로필로 지정
    public void TakeAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, REQUEST_TAKE_ALBUM);
    }

    //이미지 파일 새로 생성
    public File createImageFile() throws IOException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String currentDateandTime = simpleDateFormat.format(new Date());
        String imagefileName = "Profile_" + currentDateandTime + ".jpg";
        File imagefile = null;
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/Pictures", "WalkingDoggy");

        if (!storageDir.exists())
            storageDir.mkdir();

        imagefile = new File(storageDir, imagefileName);
        mCurrentPhotoPath = imagefile.getAbsolutePath();
        return imagefile;
    }

    //takePhoto or takeAlbum 후 이미지 크롭
    public void ImageCrop() {
        Intent intent = new Intent("com.android.camera.action.CROP");

        intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(photoURI, "image/*");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("scale", true);
        intent.putExtra("output", albumURI);
        startActivityForResult(intent, REQUEST_IMAGE_CROP);
    }

    //이미지 파일 저장
    private void galleryAddPic() {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        intent.setData(contentUri);
        sendBroadcast(intent);
        Toast.makeText(this, "사진이 앨범에 저장되었습니다.", Toast.LENGTH_LONG).show();
    }

    //프로필사진 수정 작동
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_TAKE_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    galleryAddPic();
                    profile_image.setImageURI(imageuri);
                }
                break;
            case REQUEST_TAKE_ALBUM:
                if (resultCode == Activity.RESULT_OK) {
                    if (data.getData() != null) {
                        try {
                            File albumfile = createImageFile();
                            photoURI = data.getData();
                            albumURI = Uri.fromFile(albumfile);
                            ImageCrop();
                        } catch (Exception e) {

                        }
                    }
                }
                break;
            case REQUEST_IMAGE_CROP:
                if (resultCode == Activity.RESULT_OK) {
                    galleryAddPic();
                    uploadImage(albumURI);
                    //profile_image.setImageURI(albumURI);
                }
                break;
        }
    }

    //카메라 권한 설정
    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            //퍼미션이 없는 경우
            //최초로 퍼미션을 요청하는 것인지 사용자가 취소되었던것을 다시 요청하려는건지 체크
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                //퍼미션을 재요청 하는 경우 - 왜 이 퍼미션이 필요한지등을 대화창에 넣어서 사용자를 설득할 수 있다.
                //대화상자에 '다시 묻지 않기' 체크박스가 자동으로 추가된다.
                //Log.v(TAG, "퍼미션을 재요청 합니다.");
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

            } else {
                //처음 퍼미션을 요청하는 경우
                //Log.v(TAG, "첫 퍼미션 요청입니다.");
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        } else {
            //퍼미션이 있는 경우 - 쭉 하고 싶은 일을 한다.
            //Log.v(TAG, "Permission is granted");
            //textView.setText("이미 퍼미션이 허용되었습니다.");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: //
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //사용자가 동의했을때
                    Toast.makeText(this, "퍼미션 동의되었습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    //사용자가 거부 했을때
                    Toast.makeText(this, "거부 - 동의해야 사용가능합니다.", Toast.LENGTH_SHORT).show();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, 2000);

                }
                return;
        }
    }

    //프로필 이미지 변경하고 view에 띄우기
    private void uploadImage(final Uri uri) {
        mFirebaseStorage = FirebaseStorage.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();

        if (mFirebaseAuth.getCurrentUser() != null) {
            final StorageReference storageRef = mFirebaseStorage.getReference().child(mFirebaseAuth.getCurrentUser().getUid());
            StorageReference storageReference = storageRef.child(uri.getLastPathSegment());
            storageReference.putFile(uri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    /*Glide.with(getApplicationContext())
                            .using(new FirebaseImageLoader())
                            .load(taskSnapshot.getStorage())
                            .into(profile_image);*/
                    storageUriSave(uri.getLastPathSegment());
                    finish();
                }
            });
        }
    }

    //액티비티 실행할때 기본 프로필 이미지 셋팅
    private void setProfileImage(final CircleImageView circleImageView,String string) {
        mFirebaseStorage = FirebaseStorage.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();

        if (mFirebaseAuth.getCurrentUser() != null) {
            final StorageReference storageRef = mFirebaseStorage.getReferenceFromUrl("gs://capstonedesign-41e19.appspot.com").child(mFirebaseAuth.getCurrentUser().getUid()).child(string);
            Glide.with(ProfileActivity.this)
                                .using(new FirebaseImageLoader())
                                .load(storageRef)
                                .into(circleImageView);
        }
    }

    private void storageUriSave(String string) {
        StorageUri storageUri = new StorageUri();

        storageUri.setUri(string);
        if(mFirebaseDatabase.getReference(mFirebaseAuth.getCurrentUser().getUid()).child("StorageUri") != null){
            Map<String, Object> stringObjectMap = storageUri.toMap();
            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put("/StorageUri", stringObjectMap);
            mFirebaseDatabase.getReference(mFirebaseAuth.getCurrentUser().getUid()).updateChildren(childUpdates);
        }
        else
            mFirebaseDatabase.getReference(mFirebaseAuth.getCurrentUser().getUid()).child("StorageUri").setValue(storageUri);
    }
}