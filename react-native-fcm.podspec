require "json"
package = JSON.parse(File.read('package.json'))

Pod::Spec.new do |s|
  s.name          = package['name']
  s.version       = package['version']
  s.summary       = package['description']
  s.author        = "Libin Lu"
  s.license       = package['license']
  s.requires_arc  = true
  s.homepage      = "https://github.com/evollu/react-native-fcm"
  s.source        = { :git => 'https://github.com/evollu/react-native-fcm.git' }
  s.platform      = :ios, '8.0'
  s.source_files  = "ios/*.{h,m}"

  s.dependency "React"
  s.dependency "Firebase/Messaging"
end
