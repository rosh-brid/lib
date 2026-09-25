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
