using Android.Runtime;
using Java.Interop;
using System.Diagnostics.CodeAnalysis;

namespace DotNetAndroidLib;

[Register("com/roydammarell/dotnetandroid/HelloAndroidService")]
public class HelloAndroidService : Java.Lang.Object
{
    [Register("createHello", "()Ljava/lang/String;", "")]
    [Export("createHello")]
    [UnconditionalSuppressMessage("Trimming", "IL2026", Justification = "Exported for Android Callable Wrapper binding.")]
    public virtual string CreateHello()
    {
        return "Hello from .NET 9 Android!";
    }
}
