<template>
  <q-page class="page-content" padding>
    <h5>Create user</h5>
    <q-banner v-if="error !== ''" rounded dense class="text-white bg-red-5" style="margin: 3px">
      {{ error }}
    </q-banner>
    <q-card
      flat
    >
      <user-editor-form
        v-model="newUser"
        :loading="loading"
        password-required
        @valid="isValid = true"
        @invalid="isValid = false"
      >
        <template v-slot:below>
          <div class="q-pa-md q-gutter-sm">
            <q-btn
              style="width: 100px"
              color="negative"
              label="Abort"
              no-caps
              :to="{name: 'usermanagement'}"
            />
            <q-btn
              type="submit"
              style="width: 100px"
              color="positive"
              label="Create"
              no-caps
              :disable="loading || !isValid"
              @click="createUser"
            />
          </div>
        </template>
      </user-editor-form>
    </q-card>
  </q-page>
</template>

<script>
import UserEditorForm from '../components/UserEditorForm'
import User from '../models/User'
import userService from '../services/user.service'

export default {
  name: 'UserCreator',
  components: { UserEditorForm },
  data () {
    return {
      newUser: new User('', '', '',
        '', true, '', '', 0),
      isValid: false,
      loading: false,
      error: ''
    }
  },
  methods: {
    createUser () {
      this.loading = true

      userService.createUser(this.newUser)
        .then(() => {
          this.loading = false
          this.$q.notify({
            type: 'positive',
            message: 'User created successfully'
          })
          this.$router.push({ name: 'usermanagement' })
        }).catch(error => {
          this.loading = false
          this.error = error.response.data.message
          this.$q.notify({
            type: 'negative',
            message: 'Couldn\'t create user. ' + error.response.data.message
          })
        })
    }
  }
}
</script>

<style scoped>

</style>
