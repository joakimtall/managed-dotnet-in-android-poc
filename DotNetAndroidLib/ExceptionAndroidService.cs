using Android.Runtime;
using Java.Interop;
using System.Diagnostics.CodeAnalysis;

namespace DotNetAndroidLib;

[Register("com/roydammarell/dotnetandroid/ExceptionAndroidService")]
public class ExceptionAndroidService : Java.Lang.Object
{
    [Register("throwNullReferenceException", "()V", "")]
    [Export("throwNullReferenceException")]
    [UnconditionalSuppressMessage("Trimming", "IL2026", Justification = "Exported for Android Callable Wrapper binding.")]
    public virtual void ThrowNullReferenceException()
    {
        throw new NullReferenceException("Testing C# NullReferenceException from .NET 9");
    }
}
