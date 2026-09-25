# Rosh Lib

Library Android from Rosh.

## Features
- Alert Message
- Alert Download
- Alert Unzip
- Alert File Properties
- Alert Unpac(firmware.pac)
- Alert Copy/Move
- Alert Delete

- Rosh Theme

- Click action

- Source Image(vector drawable)

- more widget (coming soon)

## How to use
- **Alert Massage**
    ```kotlin
    val msg = lib.action.Massage(this)
       msg.setTitle("My Title")
       msg.setMassage("my massage")
       msg.setPositiveButton("Run"){
           finish() //exit app
       }
       msg.setNegativeButton("cencel" , null) // or { your code}
       msg.show()
       ```
       
- **Alert Download**
    ```kotlin
    val download = lib.action.Download(this)
        download.setLink("your link")
        download.setDir("save your file in folder)
        download.setName("name file download") //opsional
        download.onFinished("your acton") //opsional maybe go to tar/unzip whitout click
        download.show()
        ```
        
- **Alert Unzip**
- **Alert FileProperti**
- **Alert Delete**
- **Alert Copy/Move**
- **Alert Unpac**

- **Click View**
   ```kotlin
   //if you use onClick or setOnClickListener
   //which click put view and action
   Click(myButton).one{
     startActivity(Intent(this, MyActivity::class.java)) //or your action
   }
   
   Click(myButton).long{
       Toast.makeText(this, "Long press button", Toast.LENGTH_SHORT).show()
   }
   
   Click(myImage).goRight{
      img.setImageResource(imgNext)
   }
   
   Click(myImg).goLeft{
      img.setImageResource(imgProfeus)
   }
   ```
   
- **Extension Icon**
    ```kotlin
    val file = File(filesDir)
    for(list in file){
        val img = ImageView(this)
        img.setImageResource(
        if(list.isDirectory){lib.R.drawable.folder}
        else{
            val icon = lib.view.IconFile(this)
            icon.setGlide(img) //opsional if your use image or video img = image view(val)
            Type(list)
        }
        )
    }
    ```
   
## Gradle Dependencies

   ```
   //kotlin
   dependencies {
       implementation("com.github.rosh-brid:lib:1.2.5")
   }
   
   //groovy
   dependencies {
      implementation 'com.github.rosh-brid:lib:1.2.5'
   }
   ```