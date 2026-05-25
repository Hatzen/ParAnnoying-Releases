See UseCases class.


Further todos
- Add test application starter
    - use volatile storage to enable all channels? Or use real settings
- write instrumentation tests
  (before or after refactoring?)
- confirmation after all data is send? (Not needed when number is present, but better to not accidentially skip a message and keep proper sync (autmation better than human failures..))
    - For QrCode, use Sound (Bluetooth, NFC would be possible as well but more likley hijacked) as confirmation, Sound could be manipulated more easily
    - For Sound, use QrCode? (Bluetooth, NFC would be possible as well but more likley hijacked) as confirmation, Sound could be manipulated more easily
    -
- Set maximum size for channel (Text 2MB, USB FAT32 4GB, NFC 3MB or somethign??, (single) QrCode 400KB?, Video with Confimration 100MB?)
- verify data (add hash of online id to number of messages with hardware encryption)


- Support single message
