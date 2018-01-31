class AppDelegate
  def application(application, didFinishLaunchingWithOptions:launchOptions)
    view = KCF<caret>
    view.layer.cornerRadius = 5.0
    transform = CGAffineTransformFromString("some_string")
    true
  end
end