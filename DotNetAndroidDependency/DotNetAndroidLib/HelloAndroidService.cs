using Android.Runtime;

namespace DotNetAndroidLib;

[Register("com/roydammarell/dotnetandroid/HelloAndroidService")]
public class HelloAndroidService : Java.Lang.Object
{
    [Register("createHello", "()Ljava/lang/String;", "")]
    public virtual string CreateHello()
    {
        return "Hello from .NET 9 Android!";
    }
}
